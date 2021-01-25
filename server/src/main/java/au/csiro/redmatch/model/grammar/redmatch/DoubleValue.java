/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
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
