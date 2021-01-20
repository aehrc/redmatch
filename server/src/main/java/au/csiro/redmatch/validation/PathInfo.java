/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Properties of a FHIR path, such as cardinality and type.
 * 
 * @author Alejandro Metke
 *
 */
public class PathInfo {
  
  private String path;
  private int min;
  private String max;
  private String type;
  private final List<String> targetProfiles = new ArrayList<>();
  
  public PathInfo (String path) {
    this.path = path;
  }

  public int getMin() {
    return min;
  }

  public void setMin(int min) {
    this.min = min;
  }

  public String getMax() {
    return max;
  }

  public void setMax(String max) {
    this.max = max;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<String> getTargetProfiles() {
    return targetProfiles;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
  
}
