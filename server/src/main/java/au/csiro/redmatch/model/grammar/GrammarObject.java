/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.model.grammar;

/**
 * Base class for all elements in the grammar.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public abstract class GrammarObject {
  /**
   * Indicates if this condition references any fields in the REDCap form.
   * 
   * @return True if any fields are referenced or false otherwise.
   */
  public abstract boolean referencesData();
}
