/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
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
