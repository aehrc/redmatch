/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import java.util.Map;

/**
 * Base class for conditions.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public abstract class Condition extends GrammarObject {

  /**
   * Indicates if this condition has been qualified with a NOT.
   */
  protected boolean negated;

  public boolean isNegated() {
    return negated;
  }

  public void setNegated(boolean negated) {
    this.negated = negated;
  }

}
