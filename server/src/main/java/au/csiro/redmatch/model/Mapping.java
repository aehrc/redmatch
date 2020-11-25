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
public class Mapping implements Comparable<Mapping> {

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
   * The free text that wants to be mapped.
   */
  private String text = "";

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
  
  /**
   * The URL of the value set that contains this code.
   */
  private String valueSetUrl;
  
  /**
   * The name of the value set that contains this code.
   */
  private String valueSetName;
  
  /**
   * Indicates if a mapping is active or not.
   */
  private boolean active = true;

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

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
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

  public String getValueSetUrl() {
    return valueSetUrl;
  }

  public void setValueSetUrl(String valueSetUrl) {
    this.valueSetUrl = valueSetUrl;
  }
  
  public boolean hasValueSetUrl() {
    return this.valueSetUrl != null;
  }

  public String getValueSetName() {
    return valueSetName;
  }

  public void setValueSetName(String valueSetName) {
    this.valueSetName = valueSetName;
  }
  
  public boolean hasValueSetName() {
    return this.valueSetName != null;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
  
  public boolean isSet() {
    return hasTargetSystem() && hasTargetCode();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((redcapFieldId == null) ? 0 : redcapFieldId.hashCode());
    result = prime * result + ((redcapFieldType == null) ? 0 : redcapFieldType.hashCode());
    result = prime * result + ((redcapLabel == null) ? 0 : redcapLabel.hashCode());
    result = prime * result + ((targetCode == null) ? 0 : targetCode.hashCode());
    result = prime * result + ((targetDisplay == null) ? 0 : targetDisplay.hashCode());
    result = prime * result + ((targetSystem == null) ? 0 : targetSystem.hashCode());
    result = prime * result + ((text == null) ? 0 : text.hashCode());
    result = prime * result + ((valueSetName == null) ? 0 : valueSetName.hashCode());
    result = prime * result + ((valueSetUrl == null) ? 0 : valueSetUrl.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Mapping other = (Mapping) obj;
    if (redcapFieldId == null) {
      if (other.redcapFieldId != null)
        return false;
    } else if (!redcapFieldId.equals(other.redcapFieldId))
      return false;
    if (redcapFieldType == null) {
      if (other.redcapFieldType != null)
        return false;
    } else if (!redcapFieldType.equals(other.redcapFieldType))
      return false;
    if (redcapLabel == null) {
      if (other.redcapLabel != null)
        return false;
    } else if (!redcapLabel.equals(other.redcapLabel))
      return false;
    if (targetCode == null) {
      if (other.targetCode != null)
        return false;
    } else if (!targetCode.equals(other.targetCode))
      return false;
    if (targetDisplay == null) {
      if (other.targetDisplay != null)
        return false;
    } else if (!targetDisplay.equals(other.targetDisplay))
      return false;
    if (targetSystem == null) {
      if (other.targetSystem != null)
        return false;
    } else if (!targetSystem.equals(other.targetSystem))
      return false;
    if (text == null) {
      if (other.text != null)
        return false;
    } else if (!text.equals(other.text))
      return false;
    if (valueSetName == null) {
      if (other.valueSetName != null)
        return false;
    } else if (!valueSetName.equals(other.valueSetName))
      return false;
    if (valueSetUrl == null) {
      if (other.valueSetUrl != null)
        return false;
    } else if (!valueSetUrl.equals(other.valueSetUrl))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Mapping [id=" + id + ", redcapFieldId=" + redcapFieldId + ", redcapLabel=" + redcapLabel
        + ", redcapFieldType=" + redcapFieldType + ", text=" + text + ", targetSystem="
        + targetSystem + ", targetCode=" + targetCode + ", targetDisplay=" + targetDisplay
        + ", valueSetUrl=" + valueSetUrl + ", valueSetName=" + valueSetName + "]";
  }

  @Override
  public int compareTo(Mapping o) {
    int res = redcapFieldId.compareTo(o.redcapFieldId);
    if (res == 0 && text != null && o.text != null) {
      return text.compareTo(o.text);
    }
    return res;
  }

}
