/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar.redmatch;

import java.util.ArrayList;
import java.util.List;

import au.csiro.redmatch.model.grammar.GrammarObject;

/**
 * Represents a resource element in the rules.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class Resource extends GrammarObject {

  /**
   * The type of resource to create.
   */
  private String resourceType;

  /**
   * The id of the resource to create.
   */
  private String resourceId;

  /**
   * The attribute in the resource to set.
   */
  private List<AttributeValue> resourceAttributeValues = new ArrayList<>();

  public Resource() {

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

  public List<AttributeValue> getResourceAttributeValues() {
    return resourceAttributeValues;
  }

  public void setResourceAttributeValues(List<AttributeValue> resourceAttributeValues) {
    this.resourceAttributeValues = resourceAttributeValues;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(resourceType);
    sb.append("<");
    sb.append(resourceId);
    sb.append("> -> ");
    if (!resourceAttributeValues.isEmpty()) { // just to avoid NPEs
      sb.append(resourceAttributeValues.get(0));
      for (int i = 1; i < resourceAttributeValues.size(); i++) {
        sb.append(", ");
        AttributeValue attVal = resourceAttributeValues.get(i);
        sb.append(attVal);
      }
    }
    return sb.toString();
  }

  @Override
  public DataReference referencesData() {
    DataReference referencesData = DataReference.NO;
    for(AttributeValue av : resourceAttributeValues) {
      switch(av.referencesData()) {
        case YES:
          referencesData = DataReference.YES;
          break;
        case RESOURCE:
          if (referencesData.equals(DataReference.NO)) {
            referencesData = DataReference.RESOURCE;
          }
          break;
        case NO:
          // Do nothing
          break;
        
        default:
          throw new RuntimeException("Unexpected value " + referencesData);
      }
    }
    
    return referencesData;
  }

}
