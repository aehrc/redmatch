/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
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

  @Override
  public DataReference referencesData() {
    return DataReference.NO;
  }
  
}
