/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
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
