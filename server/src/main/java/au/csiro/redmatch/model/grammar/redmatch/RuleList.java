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
  public DataReference referencesData() {
    DataReference referencesData = DataReference.NO;
    
    for(Rule rule : rules) {
      switch(rule.referencesData()) {
        case YES:
          referencesData = DataReference.YES;
          break;
        case RESOURCE:
          if (referencesData.equals(DataReference.NO)) {
            referencesData = DataReference.RESOURCE;
          }
          break;
        case NO:
          // Do nothing
          break;
        
        default:
          throw new RuntimeException("Unexpected value " + referencesData);
      }
    }
    
    return referencesData;
  }
  
}
