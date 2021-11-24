/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete FHIR attribute and its value, for example, 
 * Patient.identifier[2].value = '12345'.
 * 
 * @author Alejandro Metke-Jimenez
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
      sb.append(value);
    }

    return sb.toString();
  }

  @Override
  public DataReference referencesData() {
    return value != null ? value.referencesData() : DataReference.NO;
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

}
