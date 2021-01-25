/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
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
