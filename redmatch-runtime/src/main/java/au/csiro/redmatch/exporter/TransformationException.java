/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.exporter;

/**
 * Thrown when there is a runtime problem transforming source data into FHIR.
 *
 * @author Alejandro Metke Jimenez
 */
public class TransformationException extends RuntimeException {

  public TransformationException(String message) {
    super(message);
  }

  public TransformationException(String message, Throwable cause) {
    super(message, cause);
  }
}
