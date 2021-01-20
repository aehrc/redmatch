/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of a call to the Redmatch grammar validator.
 * 
 * @author Alejandro Metke
 *
 */
public class ValidationResult {

  private final boolean result;
  
  private final String code;
  
  private final List<String> messages = new ArrayList<>();
  
  public ValidationResult (boolean result, String code) {
    this.result = result;
    this.code = code;
  }
  
  public ValidationResult (boolean result, String code, String message) {
    this.result = result;
    this.code = code;
    messages.add(message);
  }
  
  public List<String> getMessages() {
    return messages;
  }

  public boolean getResult() {
    return result;
  }

  public String getCode() {
    return code;
  }

}
