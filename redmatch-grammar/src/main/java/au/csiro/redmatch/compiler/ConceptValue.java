/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

/**
 * The concept mapped to a field.
 *
 * In REDCap, this keyword applies to fields of type TEXT that have been configured to use validation based on FHIR
 * ontologies. It also applies to YESNO, DROPDOWN, RADIO, CHECKBOX, CHECKBOX_OPTION and TRUEFALSE fields. When used with
 * DROPDOWN, RADIO and CHECKBOX fields, the mapping is established to the field itself, not the options.
 *
 * @author Alejandro Metke-Jimenez
 *
 */
public class ConceptValue extends FieldBasedValue {
  
  public ConceptValue() {
    super();
  }

  public ConceptValue(String fieldId) {
    super(fieldId);
  }

  @Override
  public String toString() {
    return "CONCEPT(" + fieldId + ")";
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

}
