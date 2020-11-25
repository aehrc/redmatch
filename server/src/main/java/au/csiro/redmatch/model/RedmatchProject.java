/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import au.csiro.redmatch.util.HashUtils;

/**
 * <p>
 * Represents a Redmatch project, which is connected to a report in REDCap.
 * </p>
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Document
public class RedmatchProject {
  
  @Id
  private String id;
  
  /**
   * The id of the associated REDCap report.
   */
  private String reportId;
  
  /**
   * The URL of the REDCap API endpoint.
   */
  private String redcapUrl;

  /**
   * The token used to authenticate in the REDCap server. 
   * TODO: need to encrypt in database.
   */
  private String token;

  /**
   * The name of this project in Redmatch.
   */
  private String name;
  
  /**
   * The metadata of this REDCap project.
   */
  private Metadata metadata;

  /**
   * Any mapping rules available to generate FHIR resources.
   */
  private String rulesDocument = "";
  
  /**
   * Mappings from REDCap fields to standard terminologies.
   */
  private List<Mapping> mappings = new ArrayList<>();

  /**
   * A list of validation errors related to the transformation rules. These can happen when the
   * REDCap metadata changes and some fields referenced by the rules are removed.
   */
  private List<Annotation> issues = new ArrayList<>();
  
  /**
   * Constructor.
   */
  @JsonCreator
  public RedmatchProject(
      @JsonProperty("reportId") String reportId, 
      @JsonProperty("redcapUrl") String redcapUrl) {
    this.reportId = reportId;
    this.redcapUrl = redcapUrl;
    this.id = HashUtils.shortHash(redcapUrl) + reportId;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
  
  public boolean hasName() {
    return this.name != null;
  }

  public String getRulesDocument() {
    return rulesDocument;
  }

  public void setRulesDocument(String rulesDocument) {
    this.rulesDocument = rulesDocument;
  }
  
  public boolean hasRulesDocument() {
    return this.rulesDocument != null && !this.rulesDocument.isBlank();
  }
  
  public List<Mapping> getMappings() {
    return mappings;
  }
  
  public void addMappings (Collection<Mapping> mappings) {
    this.mappings.addAll(mappings);
  }

  public void deleteMapping(Mapping mapping) {
    this.mappings.remove(mapping);
  }
  
  public void replaceMappings (Collection<Mapping> mappings) {
    this.deleteAllMappings();
    this.addMappings(mappings);
  }

  /**
   * Deletes all {@link Mapping}s.
   */
  public void deleteAllMappings() {
    mappings.clear();
  }

  public boolean hasMappings() {
    return !mappings.isEmpty();
  }
  
  public boolean isMappingsComplete() {
    return mappings.stream()
      .map(x -> x.isSet())
      .reduce(true, Boolean::logicalAnd);
  }

  /**
   * @return the issues
   */
  public List<Annotation> getIssues() {
    return issues;
  }

  /**
   * @param issues the issues to set
   */
  public void setIssues(List<Annotation> issues) {
    this.issues = issues;
  }

  /**
   * Indicates if the project has any errors.
   * 
   * @return True if the project has errors, false otherwise.
   */
  public boolean hasErrors() {
    for (Annotation ann : issues) {
      if (ann.getAnnotationType().equals(AnnotationType.ERROR)) {
        return true;
      }
    }
    return false;
  }

  public String getReportId() {
    return reportId;
  }
  
  public boolean hasReportId() {
    return this.reportId != null;
  }

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public String getRedcapUrl() {
    return redcapUrl;
  }
  
  public boolean hasRedcapUrl() {
    return this.redcapUrl != null;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
  
  public boolean hasToken() {
    return this.token != null;
  }

  @Override
  public String toString() {
    return "RedmatchProject [id=" + id + ", reportId=" + reportId + ", redcapUrl=" + redcapUrl
        + ", token=" + token + ", name=" + name + "]";
  }

}
