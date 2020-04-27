/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
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
