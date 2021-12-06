/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import au.csiro.redmatch.compiler.*;
import au.csiro.redmatch.util.DateUtils;
import org.hl7.fhir.r4.model.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A field in a REDCap schema.
 *
 * @author Alejandro Metke-Jimenez
 */
public class RedcapField implements Field {

  public enum FieldType {
    UNKNOWN, TEXT, NOTES, DROPDOWN, RADIO, CHECKBOX, FILE, CALC, SQL, DESCRIPTIVE, SLIDER, YESNO, TRUEFALSE,
    CHECKBOX_OPTION, DROPDOW_OR_RADIO_OPTION
  }

  public enum TextValidationType {
    NONE, DATE_YMD, DATE_MDY, DATE_DMY, TIME, DATETIME_YMD, DATETIME_MDY, DATETIME_DMY, DATETIME_SECONDS_YMD,
    DATETIME_SECONDS_MDY, DATETIME_SECONDS_DMY, PHONE, EMAIL, NUMBER, INTEGER, ZIPCODE, FHIR_TERMINOLOGY
  }

  private final SimpleDateFormat dateYyyyFormat = new SimpleDateFormat("yyyy");

  private final SimpleDateFormat dateYmdFormat = new SimpleDateFormat("yyyy-MM-dd");

  private final SimpleDateFormat dateTimeYmdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private final SimpleDateFormat dateTimeSecondsYmdFormat = new SimpleDateFormat(
    "yyyy-MM-dd HH:mm:ss");

  public final Coding UNKNOWN = new Coding("http://redmatch.csiro.au", "unknown", "unknown");

  private final String fieldId;
  private final String fieldLabel;
  private final FieldType fieldType;
  private TextValidationType textValidationType;
  private final List<Field> options = new ArrayList<>();

  /**
   * Creates a text REDCap field with an optional validation.
   *
   * @param fieldId The id of the field in the schema.
   * @param textValidationType The text validation.
   */
  public RedcapField(String fieldId, String fieldLabel, TextValidationType textValidationType) {
    this(fieldId, fieldLabel, FieldType.TEXT);
    this.textValidationType = Objects.requireNonNullElse(textValidationType, TextValidationType.NONE);
  }

  /**
   * Creates a REDCap field.
   *
   * @param fieldId The id of the field in the schema.
   * @param fieldType The type of the field in the schema.
   */
  public RedcapField(String fieldId, String fieldLabel, FieldType fieldType) {
    this.fieldId = fieldId;
    this.fieldLabel = fieldLabel;
    this.fieldType = fieldType;
  }

  @Override
  public String getFieldId() {
    return fieldId;
  }

  @Override
  public String getLabel() {
    return fieldLabel;
  }

  @Override
  public boolean isCompatibleWith(FieldBasedValue expression, Collection<String> validationIssues) {
    boolean isCompatible = true;

    // CONCEPT can only apply to a field of type TEXT, YESNO, DROPDOWN, RADIO, CHECKBOX,
    // CHECKBOX_OPTION or TRUEFALSE.
    if (expression instanceof ConceptValue && !(fieldType.equals(FieldType.TEXT)
      || fieldType.equals(FieldType.YESNO) || fieldType.equals(FieldType.DROPDOWN)
      || fieldType.equals(FieldType.RADIO) || fieldType.equals(FieldType.DROPDOW_OR_RADIO_OPTION)
      || fieldType.equals(FieldType.CHECKBOX) || fieldType.equals(FieldType.CHECKBOX_OPTION)
      || fieldType.equals(FieldType.TRUEFALSE))) {
      validationIssues.add("The expression CONCEPT can only be used on fields of type TEXT, YESNO, DROPDOWN, "
          + "RADIO, DROPDOW_OR_RADIO_OPTION, CHECKBOX, CHECKBOX_OPTION or TRUEFALSE but field "
          + fieldId + " is of type " + fieldType);
      isCompatible = false;
    }

    // If used on a TEXT field, the field should be connected to a FHIR terminology server.
    if (expression instanceof ConceptValue && fieldType.equals(FieldType.TEXT)
        && !TextValidationType.FHIR_TERMINOLOGY.equals(textValidationType)) {
      validationIssues.add("The field " + fieldId + " is a text field but it is not validated using a FHIR " +
        "terminology server. CONCEPT expressions used on fields of type TEXT require that the fields are validated " +
        "using a FHIR terminology server.");
      isCompatible = false;
    }

    // CONCEPT_SELECTED can only apply to fields of type DROPDOW and RADIO
    boolean b = !(fieldType.equals(FieldType.DROPDOWN) || fieldType.equals(FieldType.RADIO));
    if (expression instanceof ConceptSelectedValue && b) {
      validationIssues.add("The expression CONCEPT_SELECTED can only be used on fields of type DROPDOWN and RADIO " +
        "but field " + fieldId + " is of type " + fieldType);
      isCompatible = false;
    }

    // CODE_SELECTED can only apply to fields of type DROPDOWN and RADIO
    if (expression instanceof CodeSelectedValue && b) {
      validationIssues.add("The expression CODE_SELECTED can only be used on fields of type DROPDOWN and RADIO but " +
        "field " + fieldId + " is of type " + fieldType);
      isCompatible = false;
    }

    return isCompatible;
  }

  @Override
  public Mapping findSelectedMapping(Map<String, Mapping> mappings, Map<String, String> row) {
    if (fieldType.equals(FieldType.RADIO) || fieldType.equals(FieldType.DROPDOWN)
      || fieldType.equals(FieldType.CHECKBOX_OPTION)) {
      String val = row.get(fieldId);
      return mappings.get(fieldId + "___" + val);
    } else {
      return null;
    }
  }

  @Override
  public Mapping findMapping(Map<String, Mapping> mappings) {
    return mappings.get(fieldId);
  }

  @Override
  public Coding getCoding(Map<String, String> row) {
    String val = row.get(fieldId);
    if (val != null && TextValidationType.FHIR_TERMINOLOGY.equals(textValidationType)) {
      String[] parts = val.split("[|]");
      if (parts.length == 3) {
        return new Coding(parts[2].trim(), parts[0].trim(), parts[1].trim());
      } else {
        return UNKNOWN;
      }
    }
    return null;
  }

  @Override
  public Base getValue(Map<String, String> row, Class<?> fhirType, FieldValue.DatePrecision pr) {
    String val = row.get(fieldId);

    switch (fieldType) {
      case TEXT:
        if (textValidationType != null) {
          switch (textValidationType) {
            case DATETIME_DMY:
            case DATETIME_MDY:
            case DATETIME_YMD:
              return getDate(val, fhirType, dateTimeYmdFormat, pr);
            case DATETIME_SECONDS_DMY:
            case DATETIME_SECONDS_MDY:
            case DATETIME_SECONDS_YMD:
              return getDate(val, fhirType, dateTimeSecondsYmdFormat, pr);
            case DATE_DMY:
            case DATE_MDY:
            case DATE_YMD:
              return getDate(val, fhirType, dateYmdFormat, pr);
            case EMAIL:
              return getString(val, fhirType, "EMAIL");
            case INTEGER:
              return getInteger(val, fhirType, pr);
            case NONE:
              return getString(val, fhirType, "TEXT");
            case NUMBER:
              return getDecimal(val, fhirType);
            case PHONE:
              return getString(val, fhirType, "PHONE");
            case TIME:
              // The constructor takes a string of the form HH:mm:ss[.SSSS] and the REDCap field is
              // HH:mm
              return new TimeType(val + ":00");
            case ZIPCODE:
              return getString(val, fhirType, "ZIPCODE");
            default:
              throw new RuntimeException("Unknown text validation type " + textValidationType);
          }
        } else {
          return getString(val, fhirType, "TEXT");
        }
      case CALC:
        // Calculations are always numbers
        return getDecimal(val, fhirType);
      case NOTES:
        return getString(val, fhirType, "NOTES");
      case RADIO:
      case DROPDOWN:
      case CHECKBOX:
      case TRUEFALSE:
      case YESNO:
      case CHECKBOX_OPTION:
      case DROPDOW_OR_RADIO_OPTION:
      case SLIDER:
      case DESCRIPTIVE:
      case FILE:
      case SQL:
      case UNKNOWN:
      default:
        throw new RuntimeException("REDCap field type " + fieldType + " is not supported in a VALUE expression(field: "
          + fieldId + ")");
    }
  }

  @Override
  public String getType() {
    return "REDCap (" + fieldType.toString()
      + (textValidationType != null ? (", " + textValidationType) : "" ) + ")";
  }

  @Override
  public boolean needsMapping(FieldBasedValue value) {
    if (value instanceof CodeSelectedValue || value instanceof ConceptSelectedValue) {
      return true;
    } else if (value instanceof ConceptValue) {
      // The only case where a mapping is not required is when this is a text field using the FHIR terminology plugin
      return !fieldType.equals(FieldType.TEXT) || !textValidationType.equals(TextValidationType.FHIR_TERMINOLOGY);
    } else {
      return false;
    }
  }

  @Override
  public List<Field> getOptions() {
    return options;
  }

  public FieldType getFieldType() {
    return fieldType;
  }

  private Base getDate(String val, Class<?> fhirType, SimpleDateFormat sdf, FieldValue.DatePrecision precision) {
    Date d = processDate(sdf, val);
    if (precision != null) {
      d = DateUtils.clear(d, precision);
    }
    if (fhirType.isAssignableFrom(InstantType.class)) {
      return new InstantType(d);
    } else if (fhirType.isAssignableFrom(DateTimeType.class)) {
      return new DateTimeType(d);
    } else if (fhirType.isAssignableFrom(DateType.class)) {
      return new DateType(d);
    } else {
      throw new CompilationException("Tried to assign REDCap DATE TIME field to FHIR "
        + "type " + fhirType.getCanonicalName() + ". Only Instant, DateTime and Date are "
        + "supported.");
    }
  }

  /**
   * Creates a date.
   *
   * @param sdf The date formatter to use.
   * @param stringVal The date in string format.
   * @return The date object.
   * @throws RuntimeException If unable to parse the date.
   */
  private Date processDate(SimpleDateFormat sdf, String stringVal) {
    Date date;
    try {
      date = sdf.parse(stringVal);
    } catch (ParseException e) {
      throw new CompilationException("Could not parse date: " + stringVal);
    }
    return date;
  }

  private Base getString(String val, Class<?> fhirType, String redcapFieldType) {
    if (fhirType.isAssignableFrom(StringType.class)) {
      return new StringType(val);
    } else {
      throw new CompilationException("Tried to assign REDCap " + redcapFieldType + " field "
        + "to FHIR type " + fhirType.getCanonicalName() + ". Only String is supported.");
    }
  }

  private Base getDecimal(String val, Class<?> fhirType) {
    if (fhirType.isAssignableFrom(DecimalType.class)) {
      return new DecimalType(val);
    } else if (fhirType.isAssignableFrom(StringType.class)) {
      // We allow assigning to a string field
      return new StringType(val);
    } else {
      throw new CompilationException("Tried to assign REDCap NUMBER field to FHIR type " +
        fhirType.getCanonicalName() + ". Only Decimal and String are supported.");
    }
  }

  private Base getInteger(String val, Class<?> fhirType, FieldValue.DatePrecision pr) {
    if (fhirType.isAssignableFrom(IntegerType.class)) {
      return new IntegerType(val);
    } else if (fhirType.isAssignableFrom(StringType.class)) {
      // We allow assigning to a string field
      return new StringType(val);
    } else if (fhirType.isAssignableFrom(DecimalType.class)) {
      // We allow assigning to a decimal field
      return new DecimalType(val);
    } else if (fhirType.isAssignableFrom(DateType.class) && pr != null && pr.equals(FieldValue.DatePrecision.YEAR)) {
      return new DateType(processDate(dateYyyyFormat, val));
    } else {
      throw new CompilationException("Tried to assign REDCap INTEGER field to FHIR type " +
        fhirType.getCanonicalName() + ". Only Integer, Decimal and String are supported.");
    }
  }
}
