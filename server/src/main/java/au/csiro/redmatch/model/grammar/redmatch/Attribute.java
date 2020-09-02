/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
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
  public boolean referencesData() {
    return false;
  }

}
