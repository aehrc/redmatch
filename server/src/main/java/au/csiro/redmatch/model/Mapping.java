/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import javax.persistence.*;

/**
 * Represents a mapping between a REDCap field to a concept in a code system.
 *
 * @author Alejandro Metke Jimenez
 *
 */
@Entity
public class Mapping implements Comparable<Mapping> {

  @Id
  @GeneratedValue(strategy= GenerationType.AUTO)
  private Long id;

  @ManyToOne
  private RedmatchProject project;

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
  private boolean inactive;

  public Mapping() {

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

  public String getValueSetUrl() {
    return valueSetUrl;
  }

  public void setValueSetUrl(String valueSetUrl) {
    this.valueSetUrl = valueSetUrl;
  }

  public String getValueSetName() {
    return valueSetName;
  }

  public void setValueSetName(String valueSetName) {
    this.valueSetName = valueSetName;
  }

  public boolean isInactive() {
    return inactive;
  }

  public void setInactive(boolean inactive) {
    this.inactive = inactive;
  }
  
  public boolean isSet() {
    return hasTargetSystem() && hasTargetCode();
  }

  public void setProject(RedmatchProject project) {
    this.project = project;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((redcapFieldId == null) ? 0 : redcapFieldId.hashCode());
    result = prime * result + ((text == null) ? 0 : text.hashCode());
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
    if (text == null) {
      return other.text == null;
    } else return text.equals(other.text);
  }

  @Override
  public String toString() {
    return "Mapping [id=" + id + ", redcapFieldId=" + redcapFieldId + ", redcapLabel=" + redcapLabel
        + ", redcapFieldType=" + redcapFieldType + ", text=" + text + ", targetSystem="
        + targetSystem + ", targetCode=" + targetCode + ", targetDisplay=" + targetDisplay
        + ", valueSetUrl=" + valueSetUrl + ", valueSetName=" + valueSetName +
      ", inactive=" + inactive + "]";
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
