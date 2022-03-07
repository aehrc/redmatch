package au.csiro.redmatch.terminology;
/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
import java.util.ArrayList;
import java.util.List;

/**
 * Properties of a code that represents a FHIR path, such as cardinality and type.
 *
 * @author Alejandro Metke-Jimenez
 */
public class CodeInfo {
  private String path;
  private int min;
  private String max;
  private String type;
  private boolean profile;
  private String baseResource;
  private String extensionUrl;
  private String profileUrl;
  private final List<String> targetProfiles = new ArrayList<>();

  public CodeInfo (String path) {
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

  public boolean isProfile() {
    return profile;
  }

  public void setProfile(boolean profile) {
    this.profile = profile;
  }

  public String getBaseResource() {
    return baseResource;
  }

  public void setBaseResource(String baseResource) {
    this.baseResource = baseResource;
  }

  public String getExtensionUrl() {
    return extensionUrl;
  }

  public void setExtensionUrl(String extensionUrl) {
    this.extensionUrl = extensionUrl;
  }

  public String getProfileUrl() {
    return profileUrl;
  }

  public void setProfileUrl(String profileUrl) {
    this.profileUrl = profileUrl;
  }
}
