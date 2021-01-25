/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node in a FHIR resource tree.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class AttributeNode {

  /**
   * The name of the attribute, e.g. gender.
   */
  private final String name;

  /**
   * Type of the attribute, e.g. code.
   */
  private final String type;

  /**
   * Indicates if the type of this attribute is abstract.
   */
  private final boolean typeAbstract;

  /**
   * True if cardinality is > 1.
   */
  private final boolean multiValued;

  /**
   * Inline, nested attributes.
   */
  private final List<AttributeNode> children = new ArrayList<>();
  
  /**
   * Creates a new attribute node.
   * 
   * @param name The name of the attribute.
   * @param type The type of the attribute.
   * @param typeAbstract Indicates if the attribute is abstract.
   * @param multiValued Indicates if the attribute is multi-valued.
   */
  public AttributeNode(String name, String type, boolean typeAbstract, boolean multiValued) {
    this.name = name;
    this.multiValued = multiValued;
    this.type = type;
    this.typeAbstract = typeAbstract;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public boolean isTypeAbstract() {
    return typeAbstract;
  }

  /**
   * Returns true if the attribute type is primitive.
   * 
   * @return True if this attribute is primitive, false otherwise.
   */
  public boolean isPrimitive() {
    if (Character.isLowerCase(type.charAt(0))) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isMultiValued() {
    return multiValued;
  }

  public List<AttributeNode> getChildren() {
    return children;
  }

  public boolean isLeaf() {
    return children.isEmpty();
  }

  @Override
  public String toString() {
    return "AttributeNode [name=" + name + ", type=" + type + ", multiValued=" + multiValued
        + ", children=" + children + "]";
  }

}
