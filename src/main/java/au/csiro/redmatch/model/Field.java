/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.model;

import org.springframework.data.annotation.Id;

/**
 * Represents a field in a REDCap form.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class Field {

  public enum FieldType {
    UNKNOWN, TEXT, NOTES, DROPDOWN, RADIO, CHECKBOX, FILE, CALC, SQL, DESCRIPTIVE, SLIDER, YESNO, 
    TRUEFALSE, CHECKBOX_OPTION, DROPDOW_OR_RADIO_OPTION
  }

  public enum TextValidationType {
    NONE, DATE_YMD, DATE_MDY, DATE_DMY, TIME, DATETIME_YMD, DATETIME_MDY, DATETIME_DMY, 
    DATETIME_SECONDS_YMD, DATETIME_SECONDS_MDY, DATETIME_SECONDS_DMY, PHONE, EMAIL, NUMBER, 
    INTEGER, ZIPCODE
  }
  
  @Id
  private String id;

  private FieldType fieldType;

  /**
   * The text validation type.
   * 
   * <table>
   * <tr>
   * <th>REDCap Text Validation</th>
   * <th>FHIR Type</th>
   * </tr>
   * <tr>
   * <td>DATE_YMD</td>
   * <td>date</td>
   * </tr>
   * <tr>
   * <td>DATE_MDY</td>
   * <td>date</td>
   * </tr>
   * <tr>
   * <td>DATE_DMY</td>
   * <td>date</td>
   * </tr>
   * <tr>
   * <td>TIME</td>
   * <td>time</td>
   * </tr>
   * <tr>
   * <td>DATETIME_YMD</td>
   * <td>dateTime</td>
   * </tr>
   * <tr>
   * <td>DATETIME_MDY</td>
   * <td>dateTime</td>
   * </tr>
   * <tr>
   * <td>DATETIME_DMY</td>
   * <td>dateTime</td>
   * </tr>
   * <tr>
   * <td>DATETIME_SECONDS_YMD</td>
   * <td>dateTime</td>
   * </tr>
   * <tr>
   * <td>DATETIME_SECONDS_MDY</td>
   * <td>dateTime</td>
   * </tr>
   * <tr>
   * <td>DATETIME_SECONDS_DMY</td>
   * <td>dateTime</td>
   * </tr>
   * <tr>
   * <td>PHONE</td>
   * <td>string</td>
   * </tr>
   * <tr>
   * <td>EMAIL</td>
   * <td>string</td>
   * </tr>
   * <tr>
   * <td>NUMBER</td>
   * <td>decimal</td>
   * </tr>
   * <tr>
   * <td>INTEGER</td>
   * <td>integer</td>
   * </tr>
   * <tr>
   * <td>ZIPCODE</td>
   * <td>string</td>
   * </tr>
   * </table>
   */
  private TextValidationType textValidationType;

  private String fieldId;

  private String fieldLabel;

  public Field() {

  }

  public FieldType getFieldType() {
    return fieldType;
  }

  public void setFieldType(FieldType fieldType) {
    this.fieldType = fieldType;
  }
  
  public boolean hasTextValidationType() {
    return textValidationType != null;
  }

  public TextValidationType getTextValidationType() {
    return textValidationType;
  }

  public void setTextValidationType(TextValidationType textValidationType) {
    this.textValidationType = textValidationType;
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getFieldLabel() {
    return fieldLabel;
  }

  public void setFieldLabel(String fieldLabel) {
    this.fieldLabel = fieldLabel;
  }

  @Override
  public String toString() {
    return "Field [id=" + id + ", fieldType=" + fieldType + ", textValidationType="
        + textValidationType + ", fieldId=" + fieldId + ", fieldLabel=" + fieldLabel + "]";
  }

}
