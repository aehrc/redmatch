/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Entity
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
   * Any mapping rules available to generate FHIR resources.
   */
  @Lob
  private String rulesDocument;

  /**
   * The list of fields in the REDCap project. This corresponds to the metadata of the project.
   */
  @OneToMany(mappedBy="project", cascade = CascadeType.ALL)
  private List<Field> fields = new ArrayList<>();
  
  /**
   * Mappings from REDCap fields to standard terminologies.
   */
  @OneToMany(mappedBy="project", cascade = CascadeType.ALL)
  private List<Mapping> mappings = new ArrayList<>();

  /**
   * A list of validation errors related to the transformation rules. These can happen when the
   * REDCap metadata changes and some fields referenced by the rules are removed.
   */
  @OneToMany(mappedBy="project", cascade = CascadeType.ALL)
  private List<Annotation> issues = new ArrayList<>();

  /**
   * Default constructor.
   */
  public RedmatchProject() {}

  /**
   * Constructor.
   *
   * @param reportId The id of the REDCap report.
   * @param redcapUrl The URL if the REDCap API.
   */
  @JsonCreator
  public RedmatchProject(
      @JsonProperty("reportId") String reportId, 
      @JsonProperty("redcapUrl") String redcapUrl) {
    this.reportId = reportId;
    this.redcapUrl = redcapUrl;
    this.id = HashUtils.shortHash(redcapUrl) + reportId;
  }

  /**
   * Constructor.
   *
   * @param reportId The id of the REDCap report.
   * @param redcapUrl The URL if the REDCap API.
   * @param token The REDCap API token.
   * @param name The name of the Redmatch project.
   */
  public RedmatchProject(
          String reportId,
          String redcapUrl,
          String token,
          String name) {
    this(reportId, redcapUrl);
    this.token = token;
    this.name = name;
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getReportId() {
    return reportId;
  }

  public void setReportId(String reportId) {
    this.reportId = reportId;
  }

  public boolean hasReportId() {
    return this.reportId != null;
  }

  public String getRedcapUrl() {
    return redcapUrl;
  }

  public void setRedcapUrl(String redcapUrl) {
    this.redcapUrl = redcapUrl;
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
    return this.rulesDocument != null;
  }

  public List<Field> getFields() {
    return fields;
  }

  public Field getField(String fieldId) {
    for (Field field : fields) {
      if (field.getFieldId().equals(fieldId)) {
        return field;
      }
    }
    return null;
  }

  /**
   * The unique field for each record is always the first field.
   *
   * @return The unique field id.
   */
  @JsonIgnore
  public String getUniqueFieldId() {
    return getFields().get(0).getFieldId();
  }

  public boolean hasField(String fieldId) {
    return getField(fieldId) != null;
  }

  public void addField(Field field) {
    field.setProject(this);
    this.fields.add(field);
  }

  public void setFields(List<Field> fields) {
    deleteAllFields();
    fields.stream().forEach(x -> addField(x));
  }

  public void deleteAllFields() {
    fields.stream().forEach(x -> x.setProject(null));
    fields.clear();
  }


  /**
   * Returns a set of all the field ids in this study. These are the names that can be referenced in
   * the transformation rules.
   *
   * @return The set of field ids for this study.
   */
  public Set<String> getFieldIds() {
    final Set<String> fieldNames = new HashSet<>();
    for (Field field : getFields()) {
      final String fieldId = field.getFieldId();
      fieldNames.add(fieldId);
    }

    return fieldNames;
  }


  public List<Mapping> getMappings() {
    return mappings;
  }

  public void addMapping(Mapping mapping) {
    mapping.setProject(this);
    this.mappings.add(mapping);
  }

  public void setMappings(Collection<Mapping> mappings) {
    deleteAllMappings();
    addMappings(mappings);
  }

  public void addMappings (Collection<Mapping> mappings) {
    mappings.stream().forEach(x -> addMapping(x));
  }
  
  public void replaceMappings (Collection<Mapping> mappings) {
    this.deleteAllMappings();
    this.addMappings(mappings);
  }

  public void deleteAllMappings() {
    mappings.stream().forEach(x -> x.setProject(null));
    mappings.clear();
  }

  public boolean hasMappings() {
    return !mappings.isEmpty();
  }
  
  public boolean isMappingsComplete() {
    return mappings.stream()
      .map(Mapping::isSet)
      .reduce(true, Boolean::logicalAnd);
  }

  public List<Annotation> getIssues() {
    return issues;
  }

  public void addIssue(Annotation issue) {
    issue.setProject(this);
    this.issues.add(issue);
  }

  public void addIssues(Collection<Annotation> issues) {
    issues.stream().forEach(x -> addIssue(x));
  }

  public void setIssues(List<Annotation> issues) {
    deleteAllIssues();
    addIssues(issues);
  }

  public void deleteAllIssues() {
    issues.stream().forEach(x -> x.setProject(null));
    issues.clear();
  }

  @Override
  public String toString() {
    return "RedmatchProject [id=" + id + ", reportId=" + reportId + ", redcapUrl=" + redcapUrl
        + ", token=" + token + ", name=" + name + "]";
  }

}
