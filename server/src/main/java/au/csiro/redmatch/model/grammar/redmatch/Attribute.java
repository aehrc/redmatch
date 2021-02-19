/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar.redmatch;

import au.csiro.redmatch.model.grammar.GrammarObject;

/**
 * Represents a single attribute of a FHIR resource. For example, the expression 
 * Patient.identifier[2].value contains two attributes, <i>identifier</i> and <i>value</i>.
 * <i>identifier</i> has attributeIndex = 2.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class Attribute extends GrammarObject {

  private String name;

  private Integer attributeIndex;
  
  private boolean list = false;

  public Attribute() {

  }

  public String getName() {
    return name;
  }

  public Attribute setName(String name) {
    this.name = name;
    return this;
  }

  public Integer getAttributeIndex() {
    return attributeIndex;
  }
  
  public boolean hasAttributeIndex() {
    return attributeIndex != null;
  }

  public Attribute setAttributeIndex(Integer attributeIndex) {
    this.attributeIndex = attributeIndex;
    return this;
  }

  public boolean isList() {
    return list;
  }

  public void setList(boolean list) {
    this.list = list;
  }

  @Override
  public String toString() {
    return "." + name + ((attributeIndex != null) ? "[" + attributeIndex + "]" : "");
  }

  @Override
  public DataReference referencesData() {
    return DataReference.NO;
  }

}
