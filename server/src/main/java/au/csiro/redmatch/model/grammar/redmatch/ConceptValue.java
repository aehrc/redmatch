/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * The concept mapped to a REDCap field. This keyword applies to fields of type TEXT that have been
 * configured to use validation based on FHIR ontologies. It also applied to YESNO, DROPDOWN, RADIO,
 * CHECKBOX, CHECKBOX_OPTION and TRUEFALSE fields. When used with DROPDOWN, RADIO and CHECKBOX 
 * fields, the mapping is establised to the field itself, not the options.
 * 
 * Free text can be mapped to a codeable concept using the VALUE keyword to set the <i>text</i> 
 * attribute.
 * 
 * @author Alejandro Metke
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
  
}
