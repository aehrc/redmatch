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
 * Represents the body of a {@link Rule}.
 * 
 * @author Alejandro Metke
 *
 */
public class Body extends GrammarObject {

  
  /**
   * A list of resources that get created if the condition evaluates to true.
   */
  private final List<Resource> resources = new ArrayList<>();
  
  /**
   * An list of rules that get evaluated if the condition evaluates to true.
   */
  private final List<Rule> rules = new ArrayList<>();

  

  /**
   * @return the resources
   */
  public List<Resource> getResources() {
    return resources;
  }

  /**
   * @return the rules
   */
  public List<Rule> getRules() {
    return rules;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((resources == null) ? 0 : resources.hashCode());
    result = prime * result + ((rules == null) ? 0 : rules.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Body other = (Body) obj;
    if (resources == null) {
      if (other.resources != null)
        return false;
    } else if (!resources.equals(other.resources))
      return false;
    if (rules == null) {
      if (other.rules != null)
        return false;
    } else if (!rules.equals(other.rules))
      return false;
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    
    sb.append("{");
    for (Resource res : resources) {
      sb.append(" ");
      sb.append(res.toString());
    }
    for (Rule rule : rules) {
      sb.append(" ");
      sb.append(rule.toString());
    }
    sb.append("}");
    
    return sb.toString();
  }
  
  public boolean referencesData() {
    boolean referencesData = false;
    
    for(Rule rule : rules) {
      referencesData = referencesData || rule.referencesData();
    }
    
    for (Resource res : resources) {
      referencesData = referencesData || res.referencesData();
    }
    
    return referencesData;
  }

}
