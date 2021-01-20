/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * The code mapped to the selected option. Only applies to fields of type DROPDOWN and RADIO. This 
 * keyword can be used to set attributes of type <i>code</i>. If a mapping is not present then the 
 * transformation fails.
 * 
 * @author Alejandro Metke
 *
 */
public class CodeSelectedValue  extends FieldBasedValue {
  public CodeSelectedValue() {
    super();
  }

  public CodeSelectedValue(String fieldId) {
    super(fieldId);
  }

  @Override
  public String toString() {
    return "CODE_SELECTED(" + fieldId + ")";
  }
}
