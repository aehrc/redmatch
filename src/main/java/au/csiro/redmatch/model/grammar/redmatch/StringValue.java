/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;

/**
 * A string literal.
 * 
 * @author Alejandro Metke
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
  
}
