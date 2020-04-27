/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * Represents an id which can potentially have a variable declaration to be used with repeat 
 * clauses. The {@link #hasVariable(String)} method should be used to check if this {@link Id} 
 * contains a variable and the {@link #evaluate(String, int)} method should only be used if this 
 * check returns true.
 * 
 * @author Alejandro Metke
 *
 */
public class Id {
  
  private String value;
  
  private String variable;
  
  /**
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * @return the variable
   */
  public String getVariable() {
    return variable;
  }

  /**
   * @param variable the variable to set
   */
  public void setVariable(String variable) {
    this.variable = variable;
  }
  
  /**
   * Returns true if this id contains a variable definition for the supplied variable name.
   * 
   * @param variable
   * @return
   */
  public boolean hasVariable(String variable) {
    return this.variable.equals(variable);
  }
  
  /**
   * Evaluates this id with a value.
   * 
   * @param variable The name of the variable, e.g. x in clin_dx_${x}.
   * @param value The value of x based on the repeats clause.
   * @return The name of the id, including the value of the variable, e.g., clin_dx_1, if x = 1.
   * 
   * @throws RuntimeException If an evaluation is attempted with the wrong variable.
   */
  public String evaluate(String variable, int value) {
    if (!this.variable.equals(variable)) {
      throw new RuntimeException("Attempted to evaluate the wrong variable. Expected " 
          + this.variable + " but got " + variable);
    }
    
    return this.value + String.valueOf(value);
  }

  
}
