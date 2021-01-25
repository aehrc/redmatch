/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
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

  @Override
  public boolean referencesData() {
    boolean referencesData = false;
    for (Rule r : rules) {
      referencesData = referencesData || r.referencesData();
    }
    return referencesData;
  }
  
}
