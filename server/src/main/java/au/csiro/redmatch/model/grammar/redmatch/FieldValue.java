/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * The value of a REDCap field. Works according to the type of field:
 * <ul>
 * <li>TEXT: the entered text.</li>
 * <li>NOTES: the entered text.</li>
 * <li>CALC: Tha value of the calculated field.</li>
 * </ul>
 * 
 * @author Alejandro Metke
 *
 */
public class FieldValue extends FieldBasedValue {

  public FieldValue() {
    super();
  }

  public FieldValue(String fieldId) {
    super(fieldId);
  }

  @Override
  public String toString() {
    return "VALUE(" + fieldId + ")";
  }
  
}
