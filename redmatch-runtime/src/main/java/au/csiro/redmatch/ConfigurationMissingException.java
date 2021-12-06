/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch;

/**
 * Thrown when attempting to run a Redmatch transformation and redmatch-config.yaml file is not available.
 *
 * @author Alejandro Metke Jimenez
 */
public class ConfigurationMissingException extends RuntimeException {

  public ConfigurationMissingException(String message) {
    super(message);
  }
}
