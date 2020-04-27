/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a mapping between a REDCap field to a concept in a code system.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Document
public class Mapping {

  @Id
  private String id;

  /**
   * The id of the REDCap field in this mapping.
   */
  private String redcapFieldId;

  /**
   * Label of the question or choice. Used to provide information to the person doing the mapping.
   */
  private String redcapLabel;

  /**
   * The type of field in REDCap. Might help with mapping process.
   */
  private String redcapFieldType;

  /**
   * The system assigned by the mapper.
   */
  private String targetSystem;

  /**
   * The code assigned by the mapper.
   */
  private String targetCode;
  
  /**
   * A representation of the meaning of the code in the system, following the rules of the system.
   */
  private String targetDisplay;

  public Mapping() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRedcapFieldId() {
    return redcapFieldId;
  }

  public void setRedcapFieldId(String redcapFieldId) {
    this.redcapFieldId = redcapFieldId;
  }

  public String getRedcapLabel() {
    return redcapLabel;
  }

  public void setRedcapLabel(String redcapLabel) {
    this.redcapLabel = redcapLabel;
  }

  public String getRedcapFieldType() {
    return redcapFieldType;
  }

  public void setRedcapFieldType(String redcapFieldType) {
    this.redcapFieldType = redcapFieldType;
  }

  public String getTargetSystem() {
    return targetSystem;
  }

  public void setTargetSystem(String targetSystem) {
    this.targetSystem = targetSystem;
  }
  
  public boolean hasTargetSystem() {
    return this.targetSystem != null;
  }

  public String getTargetCode() {
    return targetCode;
  }

  public void setTargetCode(String targetCode) {
    this.targetCode = targetCode;
  }
  
  public boolean hasTargetCode() {
    return this.targetCode != null;
  }

  public String getTargetDisplay() {
    return targetDisplay;
  }

  public void setTargetDisplay(String targetDisplay) {
    this.targetDisplay = targetDisplay;
  }
  
  public boolean hasTargetDisplay() {
    return this.targetDisplay != null;
  }

  @Override
  public String toString() {
    return "Mapping [redcapFieldId=" + redcapFieldId + ", redcapLabel=" + redcapLabel
        + ", redcapFieldType=" + redcapFieldType + ", targetSystem=" + targetSystem 
        + ", targetCode=" + targetCode + ", targetDisplay=" + targetDisplay + "]";
  }

}
