/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
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

  public enum DatePrecision { YEAR, MONTH, DAY };

  private DatePrecision datePrecision;

  public FieldValue() {
    super();
  }

  public FieldValue(String fieldId) {
    super(fieldId);
  }

  public FieldValue(String fieldId, DatePrecision datePrecision) {
    super(fieldId);
    this.datePrecision = datePrecision;
  }

  public DatePrecision getDatePrecision() {
    return datePrecision;
  }

  @Override
  public String toString() {
    return "VALUE(" + fieldId + (datePrecision != null ? ", " + datePrecision.toString() : "")+ ")";
  }
  
}
