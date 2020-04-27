/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * 
 * Thrown when attempting to replace a variable with a value but no such variable
 * is in scope.
 * 
 * @author Alejandro Metke
 *
 */
public class UnknownVariableException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public UnknownVariableException() {
    super();
  }

  public UnknownVariableException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public UnknownVariableException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnknownVariableException(String message) {
    super(message);
  }

  public UnknownVariableException(Throwable cause) {
    super(cause);
  }
  
}
