/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

/**
 * Represents an invalid field id in the schema and the closest valid id.
 *
 * @author Alejandro Metke Jimenez
 */
public class ReplacementSuggestion {
  private final String actual;
  private final String suggested;

  public ReplacementSuggestion(String actual, String suggested) {
    this.actual = actual;
    this.suggested = suggested;
  }

  public String getActual() {
    return actual;
  }

  public String getSuggested() {
    return suggested;
  }
}
