/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;

import java.util.ArrayList;
import java.util.List;

import au.csiro.redmatch.model.grammar.GrammarObject;

/**
 * Represents a list of rules. Used when compiling a repeats clause to return
 * expanded rules.
 * 
 * @author Alejandro Metke
 *
 */
public class RuleList extends GrammarObject {
  
  private final List<Rule> rules = new ArrayList<>();

  /**
   * @return the rules
   */
  public List<Rule> getRules() {
    return rules;
  }
  
}
