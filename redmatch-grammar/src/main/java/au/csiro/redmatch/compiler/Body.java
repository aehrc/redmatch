/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the body of a {@link Rule}.
 * 
 * @author Alejandro Metke-Jimenez
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
    result = prime * result + resources.hashCode();
    result = prime * result + rules.hashCode();
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
    if (!resources.equals(other.resources))
      return false;
    return rules.equals(other.rules);
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
    
    for (Resource res : resources) {
      switch(res.referencesData()) {
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

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

}
