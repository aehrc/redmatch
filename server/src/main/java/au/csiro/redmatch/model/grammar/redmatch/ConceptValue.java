/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
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
