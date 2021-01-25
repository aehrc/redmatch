/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar.redmatch;

import java.util.Map;

import au.csiro.redmatch.model.Metadata;
import au.csiro.redmatch.model.grammar.GrammarObject;

/**
 * Base class for conditions.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public abstract class Condition extends GrammarObject {

  /**
   * Indicates if this condition has been qualified with a NOT.
   */
  protected boolean negated;

  /**
   * Evaluates the condition to true or false.
   * 
   * @param metadata The metadata.
   * @param data A row of data.
   * @return True if the condition evaluates to true, false otherwise.
   */
  public abstract boolean evaluate(Metadata metadata, Map<String, String> data);

  public boolean isNegated() {
    return negated;
  }

  public void setNegated(boolean negated) {
    this.negated = negated;
  }

}
