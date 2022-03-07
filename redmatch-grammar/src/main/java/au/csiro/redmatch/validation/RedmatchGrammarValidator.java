/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.validation;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.terminology.CodeInfo;
import au.csiro.redmatch.terminology.TerminologyService;
import au.csiro.redmatch.util.ProgressReporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

/**
 * Supports validating Redmatch grammar expressions that select an attribute in the FHIR model.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class RedmatchGrammarValidator {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchGrammarValidator.class);

  private final Pattern bracketsPattern = Pattern.compile("\\[(\\d+)]");

  private final TerminologyService terminologyService;

  private final VersionedFhirPackage fhirPackage;

  private final static String INVALID_ATTRIBUTE_MESSAGE = "The attribute %s is not valid";

  public RedmatchGrammarValidator(TerminologyService terminologyService, VersionedFhirPackage fhirPackage,
                                  ProgressReporter progressReporter) {
    log.info("Initialising validator with FHIR package " + fhirPackage);
    this.terminologyService = terminologyService;
    this.fhirPackage = fhirPackage;
    terminologyService.addPackage(fhirPackage, progressReporter);
  }

  /**
   * Validates a resource (or profile) name.
   *
   * @param resourceName The name.
   * @return The validation result.
   */
  public ValidationResult validateResourceName(String resourceName) throws IOException {
    return validateCode(resourceName, resourceName, "%s is not a valid resource or profile name");
  }

  /**
   * Validates an attribute path based on the FHIR model, e.g. Patient.identifier. The path might have square brackets
   * with an index when referring to lists.
   * 
   * @param path The path to validate.
   * @return A {@link ValidationResult} object with the result of the validation.
   */
  public ValidationResult validateAttributePath (String path) throws IOException {
    // Remove any indexes
    String code = String.join("", bracketsPattern.split(path));
    
    // Check code string has no brackets
    if (code.indexOf('[') != -1) {
      return new ValidationResult(false, code, "The path " + path + " has an issue in an index (" 
          + code.indexOf('['));
    }
    
    if (code.indexOf(']') != -1) {
      return new ValidationResult(false, code, "The path " + path + " has an issue in an index (" 
          + code.indexOf(']'));
    }
    
    if (hasExtension(code)) {
      String first = getFirstExtension(code);
      ValidationResult res = validateCode(first, path, INVALID_ATTRIBUTE_MESSAGE);
      if (!res.getResult()) {
        return res;
      }
      
      List<String> others = new ArrayList<>();
      getOtherExtensions(code, others);
      
      if (others.isEmpty()) {
        return new ValidationResult(true, code);
      }
      
      for(String other : others) {
        res = validateCode(other, path, INVALID_ATTRIBUTE_MESSAGE);
        if (!res.getResult()) {
          return res;
        }
      }
      
      return new ValidationResult(true, code);
      
    } else {
      return validateCode(code, path, INVALID_ATTRIBUTE_MESSAGE);
    }
  }

  private ValidationResult validateCode(String code, String path, String message) throws IOException {
    // Special case: codes ending with extension.url
    if (code.endsWith("extension.url")) {
      code = code.substring(0, code.length() - 4);
    }
    
    Boolean res = null;

    Parameters out = terminologyService.validate(fhirPackage, code);
    for (ParametersParameterComponent param : out.getParameter()) {
      if (param.getName().equals("result")) {
        res = ((BooleanType) param.getValue()).getValue();
      }
    }
    if (res == null) {
      throw new RuntimeException("Unexpected response (has no 'result' out parameter).");
    }
    ValidationResult vr = new ValidationResult(res, code);
    if (!res) {
      vr.getMessages().add(String.format(message, path));
    }
    return vr;
  }
  
  /**
   * Returns true if a code contains one or more extensions.
   * 
   * @param code The code.
   * @return True if it has extensions, false otherwise.
   */
  private boolean hasExtension(String code) {
    int dotIndex = code.indexOf('.');
    int extensionIndex = -1;
    if (dotIndex != -1) {
      extensionIndex = code.substring(dotIndex).indexOf("extension");
    }

    return dotIndex != -1 && extensionIndex != -1;
  }
  
  /**
   * Given a code with an extension, it returns the first extension, which belongs to a resource. 
   * For example:
   * 
   * <ol>
   *   <li>Encounter.extension -> Encounter.extension</li>
   *   <li>Encounter.extension.valueQuantity.extension.url -> Encounter.extension</li>
   *   <li>Encounter.extension.valueQuantity.system -> Encounter.extension</li>
   *   <li>Encounter.extension.url -> Encounter.extension.url</li>
   *   <li>Encounter.code -> undefined behaviour</li>
   * </ol>
   * 
   * @param code The code.
   * @return The first extension.
   */
  String getFirstExtension(String code) {
    int dotIndex = code.indexOf('.');
    
    int extensionIndex = code.substring(dotIndex).indexOf("extension") + dotIndex;
    String res = code.substring(0, extensionIndex + 9);
    // Special case - look for extension.url
    if (code.length() - res.length() >= 4) {
      String suffix = code.substring(res.length(), res.length() + 4);
      if (suffix.equals(".url")) {
        return res + ".url";
      }
    }
    return res;
  }
  
  /**
   * Given a code with an extension, return a list of additional codes that need to be checked 
   * against complex types. These can include nested extensions. For example:
   * 
   * <ol>
   *   <li>Encounter.extension -> empty</li>
   *   <li>Encounter.extension.valueQuantity.extension.url -> [Quantity.extension.url]</li>
   *   <li>Encounter.extension.valueQuantity.system -> [Quantity.system]</li>
   *   <li>Encounter.extension.url -> empty</li>
   *   <li>Observation.valueRatio.extension.valueRatio.extension.url -> [Ratio.extension, 
   *   Ratio.extension.url]</li>
   *   <li>Encounter.extension.valueQuantity.extension.valueQuantity.extension.valueQuantity.system 
   *   -> [Quantity.extension, Quantity.extension, Quantity.system]</li>
   * </ol>
   * 
   * @param code The code.
   * @param res List of additional codes that need to be checked against complex types.
   */
  void getOtherExtensions(String code, List<String> res) {
    // Get rid of first extension
    String firstExtension = getFirstExtension(code);
    code = code.substring(firstExtension.length());
    
    if (code.isEmpty()) {
      return;
    }
    
    if (code.startsWith(".value")) {
      code = code.substring(6);
    }
    
    if (!hasExtension(code)) {
      res.add(code);
    } else {
      String nextExtension = getFirstExtension(code);
      res.add(nextExtension);
      getOtherExtensions(code, res);
    }
  }
  
  /**
   * Returns properties of a FHIR path. This method assumes the path is valid.
   * 
   * @param path The path.
   * @return The properties of the path.
   */
  public CodeInfo getPathInfo(String path) throws IOException {
    if (hasExtension(path)) {
      String first = getFirstExtension(path);
      List<String> others = new ArrayList<>();
      getOtherExtensions(path, others);
      
      if (others.isEmpty()) {
        return getInfo(first);
      } else {
        // return info for last element
        // special case extension.value
        String newPath = others.get(others.size() - 1);
        // If the new path does not contain a dot then the path info corresponds to extension.value
        // e.g. extension.valueReference
        if (!newPath.contains(".")) {
          return handleExtensionValue(path);
        }
        return getInfo(newPath);
      }
    } else {
      return getInfo(path);
    }
  }
  
  private CodeInfo getInfo(String path) throws IOException {
    if (hasExtension(path)) {
      String first = getFirstExtension(path);
      List<String> others = new ArrayList<>();
      getOtherExtensions(path, others);

      if (others.isEmpty()) {

        // Special case: codes ending with extension.url
        if (first.endsWith("extension.url")) {
          return handleExtensionUrl(path);
        }

        return terminologyService.lookup(fhirPackage, first);
      } else {

        String last = others.get(others.size() - 1);

        // Special case: codes ending with extension.url
        if (last.endsWith("extension.url")) {
          return handleExtensionUrl(path);
        }

        return terminologyService.lookup(fhirPackage, last);
      }
    } else {
      return terminologyService.lookup(fhirPackage, path);
    }
  }
  
  private CodeInfo handleExtensionValue(String path) {
    CodeInfo res = new CodeInfo(path);
    res.setMin(0);
    res.setMax("1");
    res.setType("");
    
    return res;
  }
  
  private CodeInfo handleExtensionUrl(String path) {
    CodeInfo res = new CodeInfo(path);
    res.setMin(1);
    res.setMax("1");
    res.setType("uri");
    
    return res;
  }

}
