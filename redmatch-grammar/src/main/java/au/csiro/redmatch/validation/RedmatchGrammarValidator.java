/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.validation;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import au.csiro.ontoserver.api.InternalApi;
import au.csiro.redmatch.compiler.PathInfo;
import au.csiro.redmatch.util.DateUtils;
import au.csiro.redmatch.util.FileUtils;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.JsonParser;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Supports validating Redmatch grammar expressions that select an attribute in the FHIR model.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
@Component
public class RedmatchGrammarValidator {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchGrammarValidator.class);

  private static final String RESOURCES_URL = "http://csiro.au/redmatch-fhir/resources";

  private static final String TYPES_URL = "http://csiro.au/redmatch-fhir/complex-types";

  private final Pattern bracketsPattern = Pattern.compile("\\[(\\d+)]");

  private final InternalApi onto;

  @Autowired
  public RedmatchGrammarValidator(Gson gson, FhirContext ctx) {
    log.info("Initialising validator");
    Instant start = Instant.now();
    onto = new InternalApi(gson, ctx);
    try {
      init();
      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      log.info("Done initialising validator in: " + DateUtils.prettyPrintMillis(timeElapsed));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void init() throws IOException {
    CodeSystem[] codeSystems = new CodeSystem[2];
    if (!onto.isIndexed(RESOURCES_URL, "1.0") || !onto.isIndexed(TYPES_URL, "1.0")) {
      // TODO: update this to use the new code system generator
      //RedmatchGrammarCodeSystemGenerator gen = new RedmatchGrammarCodeSystemGenerator();
      //FhirVersionEnum version = FhirVersionEnum.R4;
      //codeSystems = gen.createCodeSystems(loadTypesBundle(version), loadResourcesBundle(version));
    }

    // Check if code systems are already indexed
    if (!onto.isIndexed(RESOURCES_URL, "1.0")) {
      log.info("Initialising validator for the first time");
      log.debug("Loading FHIR resources code system");
      CodeSystem resourcesCodeSystem = codeSystems[0];
      assert resourcesCodeSystem != null;
      onto.indexFhirCodeSystem(resourcesCodeSystem);
      log.info("Indexed resources code system");
    } else {
      log.debug("Code system is present");
    }

    if (!onto.isIndexed(TYPES_URL, "1.0")) {
      // Load complex types code system
      log.debug("Loading FHIR complex types code system");
      CodeSystem typesCodeSystem = codeSystems[1];
      assert typesCodeSystem != null;
      onto.indexFhirCodeSystem(typesCodeSystem);
      log.info("Indexed types code system");
    } else {
      log.debug("Code system is present");
    }
  }

  private Bundle loadResourcesBundle(FhirVersionEnum version) throws IOException {
    File profilesResources = FileUtils.loadFileFromClassPath("fhir-metadata/4.0.1/profiles-resources.json");
    log.info("Loading FHIR resources from " + profilesResources.getAbsolutePath());

    FhirContext ctx = new FhirContext(version);
    JsonParser parser = (JsonParser) ctx.newJsonParser().setPrettyPrint(true);
    try (FileReader fr = new FileReader(profilesResources)) {
      return parser.doParseResource(Bundle.class, fr);
    }
  }

  private Bundle loadTypesBundle(FhirVersionEnum version) throws IOException {
    File profilesTypes = FileUtils.loadFileFromClassPath("fhir-metadata/4.0.1/profiles-types.json");
    log.info("Loading FHIR resources from " + profilesTypes.getAbsolutePath());

    FhirContext ctx = new FhirContext(version);
    JsonParser parser = (JsonParser) ctx.newJsonParser().setPrettyPrint(true);
    try (FileReader fr = new FileReader(profilesTypes)) {
      return parser.doParseResource(Bundle.class, fr);
    }
  }

  /**
   * Validates an attribute path based on the FHIR model, e.g. Patient.identifier. The path might have square brackets
   * with an index when referring to lists.
   * 
   * @param path The path to validate.
   * @return A {@link ValidationResult} object with the result of the validation.
   */
  public ValidationResult validateAttributePath (String path) {
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
      ValidationResult res = validateCode(first, path, true);
      if (!res.getResult()) {
        return res;
      }
      
      List<String> others = new ArrayList<>();
      getOtherExtensions(code, others);
      
      if (others.isEmpty()) {
        return new ValidationResult(true, code);
      }
      
      for(String other : others) {
        res = validateCode(other, path, false);
        if (!res.getResult()) {
          return res;
        }
      }
      
      return new ValidationResult(true, code);
      
    } else {
      return validateCode(code, path, true);
    }
  }
  
  private ValidationResult validateCode(String code, String path, boolean isResource) {
    // Special case: codes ending with extension.url
    if (code.endsWith("extension.url")) {
      code = code.substring(0, code.length() - 4);
    }
    
    Boolean res = null;
    String url = isResource ? "http://csiro.au/redmatch-fhir/resources" : 
      "http://csiro.au/redmatch-fhir/complex-types";

    try {
      Parameters out = onto.validateCode(url, "1.0", code, null);
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
        vr.getMessages().add("The path " + path + " is not valid.");
      }
      return vr;
    } catch (IOException e) {
      throw new RuntimeException("There was an issue calling Ontoserver.", e);
    }
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
  public PathInfo getPathInfo(String path) {
    if (hasExtension(path)) {
      String first = getFirstExtension(path);
      List<String> others = new ArrayList<>();
      getOtherExtensions(path, others);
      
      if (others.isEmpty()) {
        return getInfo(first, true);
      } else {
        // return info for last element
        // special case extension.value
        String newPath = others.get(others.size() - 1);
        // If the new path does not contain a dot then the path info corresponds to extension.value
        // e.g. extension.valueReference
        if (!newPath.contains(".")) {
          return handleExtensionValue(path);
        }
        return getInfo(newPath, false);
      }
    } else {
      return getInfo(path, true);
    }
  }
  
  private PathInfo getInfo(String path, boolean isResource) {
    try {
      String url = isResource ? RESOURCES_URL : TYPES_URL;

      if (hasExtension(path)) {
        String first = getFirstExtension(path);
        List<String> others = new ArrayList<>();
        getOtherExtensions(path, others);

        if (others.isEmpty()) {

          // Special case: codes ending with extension.url
          if (first.endsWith("extension.url")) {
            return handleExtensionUrl(path);
          }

          Parameters out = onto.lookup(url, "1.0", first, Arrays.asList("min", "max", "type", "targetProfile"));
          return getPathInfo(path, out);
        } else {

          String last = others.get(others.size() - 1);

          // Special case: codes ending with extension.url
          if (last.endsWith("extension.url")) {
            return handleExtensionUrl(path);
          }

          Parameters out = onto.lookup(url, "1.0", last, Arrays.asList("min", "max", "type", "targetProfile"));
          return getPathInfo(path, out);
        }
      } else {
        Parameters out = onto.lookup(url, "1.0", path, Arrays.asList("min", "max", "type", "targetProfile"));
        return getPathInfo(path, out);
      }
    } catch (IOException e) {
      throw new RuntimeException("There was an issue calling Ontoserver.", e);
    }
  }
  
  private PathInfo handleExtensionValue(String path) {
    PathInfo res = new PathInfo(path);
    res.setMin(0);
    res.setMax("1");
    res.setType("");
    
    return res;
  }
  
  private PathInfo handleExtensionUrl(String path) {
    PathInfo res = new PathInfo(path);
    res.setMin(1);
    res.setMax("1");
    res.setType("uri");
    
    return res;
  }
  
  private PathInfo getPathInfo(String path, Parameters out) {
    PathInfo res = new PathInfo(path);
    for(ParametersParameterComponent param : out.getParameter()) {
      if (param.getName().equals("property")) {
        List<ParametersParameterComponent> ppcs = param.getPart();
        if (ppcs.size() == 2) {
          ParametersParameterComponent code = ppcs.get(0);
          if ("code".equals(code.getName())) {
            String codeValue = ((StringType) code.getValue()).getValue();
            ParametersParameterComponent value = ppcs.get(1);
            if (value.getName().startsWith("value")) {
              if ("min".equals(codeValue)) {
                res.setMin(((IntegerType) value.getValue()).getValue());
              } else if ("max".equals(codeValue)) {
                res.setMax(((StringType) value.getValue()).getValue());
              } else if ("type".equals(codeValue)) {
                res.setType(((StringType) value.getValue()).getValue());
              } else if ("targetProfile".equals(codeValue)) {
                res.getTargetProfiles().add(((StringType) value.getValue()).getValue());
              } else {
                log.warn("Unexpected property: " + codeValue);
              }
            }
          }
        }
      }
    }
    return res;
  }

}
