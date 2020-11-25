/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;

/**
 * A double literal.
 * 
 * @author Alejandro Metke
 *
 */
public class DoubleValue extends Value {
  private Double value;
  
  public DoubleValue() {
    super();
  }

  public DoubleValue(Double value) {
    super();
    this.value = value;
  }

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString(); // TODO: might need to format this
  }

  @Override
  public boolean referencesData() {
    return false;
  }
  
}
