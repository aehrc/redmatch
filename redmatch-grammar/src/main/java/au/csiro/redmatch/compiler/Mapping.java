/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import org.hl7.fhir.r4.model.Coding;

/**
 * Represents a mapping between a field in the schema and a {@link Coding}.
 *
 * @author Alejandro Metke-Jimenez
 */
public class Mapping extends GrammarObject {

  private final String fieldId;
  private final Coding target;

  public Mapping(String fieldId, String system, String code, String display) {
    this.fieldId = fieldId;
    this.target = new Coding(system, code, display);
  }

  public String getFieldId() {
    return fieldId;
  }

  public Coding getTarget() {
    return target;
  }

  @Override
  public DataReference referencesData() {
    return DataReference.NO;
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

  @Override
  public String toString() {
    return "Mapping{" +
      "fieldId='" + fieldId + '\'' +
      ", target=" + target +
      '}';
  }
}
