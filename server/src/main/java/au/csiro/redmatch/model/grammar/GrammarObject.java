/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar;

/**
 * Base class for all elements in the grammar.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public abstract class GrammarObject {
  
  public enum DataReference {
    YES,
    NO,
    RESOURCE
  }
  
  /**
   * Indicates if this condition references any fields in the REDCap form.
   * 
   * @return True if any fields are referenced or false otherwise.
   */
  public abstract DataReference referencesData();
}
