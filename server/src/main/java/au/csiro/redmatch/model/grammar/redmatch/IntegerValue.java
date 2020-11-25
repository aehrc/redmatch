/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;

/**
 * An integer literal.
 * 
 * @author Alejandro Metke
 *
 */
public class IntegerValue extends Value {
  
  private Integer value;
  
  public IntegerValue() {
    
  }
  
  public IntegerValue(Integer value) {
    super();
    this.value = value;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public boolean referencesData() {
    return false;
  }
  
}
