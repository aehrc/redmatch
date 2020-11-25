/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * Value types that reference a REDCap field.
 * 
 * @author Alejandro Metke
 *
 */
public abstract class FieldBasedValue extends Value {
  protected String fieldId;
  
  public FieldBasedValue() {
    super();
  }
  
  public FieldBasedValue(String fieldId) {
    super();
    this.fieldId = fieldId;
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }
  
  @Override
  public boolean referencesData() {
    return true;
  }
}
