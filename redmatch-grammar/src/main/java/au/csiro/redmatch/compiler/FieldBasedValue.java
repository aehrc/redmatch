/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;


/**
 * Value types that reference a field in the data source.
 * 
 * @author Alejandro Metke-Jimenez
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
  public DataReference referencesData() {
    return DataReference.YES;
  }
}
