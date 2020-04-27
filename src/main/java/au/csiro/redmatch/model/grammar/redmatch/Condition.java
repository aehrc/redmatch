/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
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
