/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * Boolean literal.
 * 
 * @author Alejandro Metke
 *
 */
public class BooleanValue extends Value {
  
  private boolean value;
  
  public BooleanValue() {
    
  }

  public BooleanValue(boolean b) {
    this.value = b;
  }

  public boolean getValue() {
    return value;
  }

  public void setValue(boolean value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return Boolean.toString(value).toUpperCase();
  }
  
}
