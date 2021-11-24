/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

/**
 * Base class for all elements in the grammar.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public abstract class GrammarObject {
  
  public enum DataReference {
    YES,
    NO,
    RESOURCE
  }
  
  /**
   * Indicates if this grammar object references any data fields or other FHIR resources.
   * 
   * @return YES if other fields are referenced, RESOURCE if another FHIR resource is referenced and NO if none are
   * referenced.
   */
  public abstract DataReference referencesData();

  /**
   * Used to accept a visitor in the grammar nodes.
   *
   * @param v The visitor.
   */
  public abstract void accept(GrammarObjectVisitor v);
}
