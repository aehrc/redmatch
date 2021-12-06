/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch;

/**
 * Enumeration for API operations.
 *
 * @author Alejandro Metke Jimenez
 */
public enum Operation {
  EXPORT ("export operation"),
  GENERATE_GRAPH ("graph generation operation"),
  BOTH ("export and graph generation operation");

  private final String name;

  Operation(String s) {
    name = s;
  }

  public String toString() {
    return this.name;
  }
}
