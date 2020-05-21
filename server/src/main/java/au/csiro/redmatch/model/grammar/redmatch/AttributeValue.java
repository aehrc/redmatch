/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.model.grammar.redmatch;

import java.util.ArrayList;
import java.util.List;

import au.csiro.redmatch.model.grammar.GrammarObject;

/**
 * Represents a complete FHIR attribute and its value, for example, 
 * Patient.identifier[2].value = '12345'.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class AttributeValue extends GrammarObject {

  private List<Attribute> attributes = new ArrayList<>();

  private Value value;

  public AttributeValue() {

  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    this.value = value;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();

    for (Attribute att : attributes) {
      sb.append(att.toString());
    }

    sb.append(" = ");

    if (value != null) {
      sb.append(value.toString());
    }

    return sb.toString();
  }

}
