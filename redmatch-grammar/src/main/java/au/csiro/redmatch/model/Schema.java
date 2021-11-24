/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents the schema of a data source that wants to be transformed to FHIR.
 *
 * @author Alejandro Metke-Jimenez
 */
public class Schema {

  public enum SchemaType { REDCAP, DB };

  private final List<Field> fields = new ArrayList<>();

  private final SchemaType schemaType;

  public Schema(SchemaType schemaType) {
    this.schemaType = schemaType;
  }

  public boolean hasField(String fieldId) {
    Optional<Field> result = findField(fieldId);
    return result.isPresent();
  }

  public Field getField(String fieldId) {
    Optional<Field> result = findField(fieldId);
    return result.orElse(null);
  }

  public List<Field> getFields() {
    return fields;
  }

  private Optional<Field> findField(String fieldId) {
    return fields
      .stream().parallel()
      .filter(f -> fieldId.equals(f.getFieldId())).findAny();
  }

  public SchemaType getSchemaType() {
    return schemaType;
  }
}
