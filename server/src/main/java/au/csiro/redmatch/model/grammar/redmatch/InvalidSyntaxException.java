/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * @author Alejandro Metke
 *
 */
public class InvalidSyntaxException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidSyntaxException() {
    
  }

  public InvalidSyntaxException(String message) {
    super(message);
  }

  public InvalidSyntaxException(Throwable cause) {
    super(cause);
  }

  public InvalidSyntaxException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidSyntaxException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
