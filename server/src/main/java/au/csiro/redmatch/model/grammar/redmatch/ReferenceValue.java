/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 *  A reference to a FHIR resource defined in the rules.
 *  
 * @author Alejandro Metke
 *
 */
public class ReferenceValue extends Value {
  /**
   * This attribute holds the type of resource that is referenced.
   */
  private String resourceType;

  /**
   * This attribute holds the id of the FHIR resource.
   */
  private String resourceId;

  public ReferenceValue() {
    
  }
  
  public ReferenceValue(String resourceType, String resourceId) {
    super();
    this.resourceType = resourceType;
    this.resourceId = resourceId;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  @Override
  public String toString() {
    return "REF(" + resourceType + "<" + resourceId + ">)";
  }

  @Override
  public DataReference referencesData() {
    return DataReference.RESOURCE;
  }
  
}
