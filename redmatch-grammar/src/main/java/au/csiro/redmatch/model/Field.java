/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import au.csiro.redmatch.compiler.FieldBasedValue;
import au.csiro.redmatch.compiler.FieldValue;
import au.csiro.redmatch.compiler.Mapping;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Coding;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This interface represents a field in a data source schema.
 *
 * @author Alejandro Metke-Jimenez
 */
public interface Field {

  /**
   * Returns the id of the field.
   *
   * @return The id of the field.
   */
  String getFieldId();

  /**
   * Returns the label of the field.
   *
   * @return The label of the field or null if it has no label.
   */
  String getLabel();

  /**
   * Indicates if this field is compatible with a certain Redmatch expression.
   *
   * @param expression The Redmatch expression.
   * @param validationIssues A collection used to return detailed validation issues. Can be null.
   * @return True if the field is compatible, false otherwise.
   */
  boolean isCompatibleWith(FieldBasedValue expression, Collection<String> validationIssues);

  /**
   * Finds the selected mapping for this field.
   *
   * @param mappings The mappings.
   * @param row The row that is in scope.
   * @return The selected mapping for this field or null if no such mapping exists.
   */
  Mapping findSelectedMapping(Map<String, Mapping> mappings, Map<String, String> row);

  Mapping findMapping(Map<String, Mapping> mappings);

  /**
   * If this field captures coded data then this method returns the selected code for a specific row of data. Otherwise
   * it returns null.
   *
   * @param row The row of data.
   * @return The {@link Coding} that was selected or null if this field does not capture coded data.
   */
  Coding getCoding(Map<String, String> row);

  /**
   * Returns the value of this field.
   *
   * @param row The row of data.
   * @param fhirType The type of the FHIR attribute where this value will be set.
   * @param pr The precision of dates. Can be null. Used for de-identification.
   * @return The value of the field. Can be null if there is no value for the field.
   */
  Base getValue(Map<String, String> row, Class<?> fhirType, FieldValue.DatePrecision pr);

  /**
   * Returns the field type.
   *
   * @return The field type.
   */
  String getType();

  /**
   * Determines if this field required a mapping in the transformation rules document.
   *
   * @param value The value that references this field.
   * @return true if the field requires a mapping.
   */
  boolean needsMapping(FieldBasedValue value);

  /**
   * Returns a list of fields that are options of this field. For example, in REDCap, fields are created to represent
   * options of combo boxes. Calling this method on a field of type combo box will the fields that are created for its
   * options.
   *
   * @return a list of options or an empty list if there are no options associated with this field.
   */
  List<Field> getOptions();
}
