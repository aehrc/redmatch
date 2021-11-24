/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

/**
 * An integer literal.
 * 
 * @author Alejandro Metke-Jimenez
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
  public DataReference referencesData() {
    return DataReference.NO;
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

}
