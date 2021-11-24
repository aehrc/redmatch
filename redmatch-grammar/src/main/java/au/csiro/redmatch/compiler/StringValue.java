/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

/**
 * A string literal.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class StringValue extends Value {
  private String stringValue;
  
  public StringValue() {
    super();
  }

  public StringValue(String s) {
    this.stringValue = s;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  @Override
  public String toString() {
    return "'" + stringValue + "'";
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
