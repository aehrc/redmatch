/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar.redmatch;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

/**
 * The concept mapped to the selected option. Only applies to fields of type DROPDOWN and RADIO. 
 * This keyword can be used to set attributes of type {@link Coding} and {@link CodeableConcept}. If
 * a mapping is not present then the transformation fails.
 * 
 * @author Alejandro Metke
 *
 */
public class ConceptSelectedValue extends FieldBasedValue {
  
  public ConceptSelectedValue() {
    super();
  }

  public ConceptSelectedValue(String fieldId) {
    super(fieldId);
  }

  @Override
  public String toString() {
    return "CONCEPT_SELECTED(" + fieldId + ")";
  }

}
