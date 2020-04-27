/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
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
  
}
