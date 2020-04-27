/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;

import au.csiro.redmatch.model.grammar.GrammarObject;

/**
 * @author Alejandro Metke
 *
 */
public class VariableIdentifier  extends GrammarObject {

  private String id;
  
  private Integer variable;
  
  public VariableIdentifier(String id) {
    super();
    this.id = id;
  }
  
  public VariableIdentifier(String id, Integer variable) {
    super();
    this.id = id;
    this.variable = variable;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getVariable() {
    return variable;
  }

  public void setVariable(Integer variable) {
    this.variable = variable;
  }
  
  public String getFullId() {
    if (variable != null) {
      return  id + variable.toString();
    } else {
      return id;
    }
  }

}
