/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.validation;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import au.csiro.redmatch.client.ITerminologyServer;
import au.csiro.redmatch.client.OntoserverClient;

/**
 * Thin layer over {@link OntoserverClient} that supports validating Redmatch grammar expressions
 * that select an attribute in the FHIR model.
 * 
 * @author Alejandro Metke
 *
 */
@Component
public class RedmatchGrammarValidator {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchGrammarValidator.class);
  
  private ITerminologyServer client;
  
  private Pattern bracketsPattern = Pattern.compile("\\[(\\d+)\\]");
  
  @Autowired
  public RedmatchGrammarValidator(@Qualifier("ontoserver")ITerminologyServer client) {
    this.client = client;
  }
  
  /**
   * Gets the terminology server client used to validate.
   * 
   * @return
   */
  public ITerminologyServer getClient() {
    return client;
  }
  
  /**
   * Sets the terminology server client used to validate.
   * 
   * @param client
   */
  public void setClient(ITerminologyServer client) {
    this.client = client;
  }

  /**
   * Validates an attribute path based on the FHIR model, e.g. Patient.identifier. The path might
   * have square brackets with an index when referring to lists.
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
    
    // Validate the code with Ontoserver
    Boolean res = null;
    Parameters out = client.validateCode("http://csiro.au/redmatch-fhir", code);
    
    for(ParametersParameterComponent param : out.getParameter()) {
        if (param.getName().equals("result")) {
          res = ((BooleanType) param.getValue()).getValue();
        }
    }
    
    if (res == null) {
      throw new RuntimeException("Unexpected response (has no 'result' out parameter).");
    }
    
    ValidationResult vr = new ValidationResult(res.booleanValue(), code);
    if (!res) {
      vr.getMessages().add("The path " + path + " is not valid.");
    }
    return vr;
  }
  
  /**
   * Returns properties of a FHIR path. This method assumes the path is valid.
   * 
   * @param path The path.
   * @return The properties of the path.
   */
  public PathInfo getPathInfo (String path) {
    Parameters out = client.lookup("http://csiro.au/redmatch-fhir", path, 
        Arrays.asList("min", "max", "type", "targetProfile"));
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
                  res.setMin(((IntegerType) value.getValue()).getValue().intValue());
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
