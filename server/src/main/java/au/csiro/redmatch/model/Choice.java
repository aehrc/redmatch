/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model;

/**
 * Represents a choice in an element, for example, the choices in a dropdown box.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class Choice implements Comparable<Choice> {

  private String redcapCode;

  private String label;

  public Choice() {

  }

  public String getRedcapCode() {
    return redcapCode;
  }

  public void setRedcapCode(String redcapCode) {
    this.redcapCode = redcapCode;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((redcapCode == null) ? 0 : redcapCode.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Choice other = (Choice) obj;
    if (redcapCode == null) {
      if (other.redcapCode != null) {
        return false;
      }
    } else if (!redcapCode.equals(other.redcapCode)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Choice [redcapCode=" + redcapCode + ", label=" + label + "]";
  }

  @Override
  public int compareTo(Choice o) {
    return redcapCode.compareTo(o.redcapCode);
  }

}
