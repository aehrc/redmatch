/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import au.csiro.redmatch.compiler.RedmatchCompiler;
import au.csiro.redmatch.exceptions.RedmatchException;
import au.csiro.redmatch.exporter.ExcelExporter;
import au.csiro.redmatch.exporter.FhirExporter;
import au.csiro.redmatch.importer.CompilerException;
import au.csiro.redmatch.importer.ExcelImporter;
import au.csiro.redmatch.importer.RedcapImporter;
import au.csiro.redmatch.model.Annotation;
import au.csiro.redmatch.model.AnnotationType;
import au.csiro.redmatch.model.RedmatchProject;
import au.csiro.redmatch.model.Mapping;
import au.csiro.redmatch.model.Metadata;
import au.csiro.redmatch.model.OperationResponse;
import au.csiro.redmatch.model.Row;
import au.csiro.redmatch.model.OperationResponse.RegistrationStatus;
import au.csiro.redmatch.model.grammar.redmatch.Document;
import au.csiro.redmatch.persistence.RedmatchDao;
import au.csiro.redmatch.util.ReflectionUtils;

/**
 * Main API for Redmatch.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Component(value = "api")
public class RedmatchApi {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchApi.class);

  @Autowired
  private RedcapImporter redcapImporter;

  @Autowired
  private ExcelExporter excelExporter;

  @Autowired
  private ExcelImporter excelImporter;

  @Autowired
  private RedmatchCompiler compiler;

  @Autowired
  private FhirExporter fhirExporter;

  @Autowired
  private RedmatchDao dao;

  /**
   * Cretaes a new Redmatch project.
   * 
   * @param fp The Redmatch project to create. The attributes <i>name</i>, <i>reportId</i>, 
   * <i>redcapUrl</i> and <i>token</i> should be set.
   * @return {@link OperationResponse} indicating if the project was registered or 
   *     already existed.
   */
  @Transactional
  public OperationResponse createRedmatchProject(RedmatchProject fp) {
    if (!fp.hasName()) {
      throw new InvalidProjectException("Attribute 'name' is required.");
    }
    
    if (!fp.hasReportId()) {
      throw new InvalidProjectException("Attribute 'reportId' is required.");
    }
    
    if (!fp.hasRedcapUrl()) {
      throw new InvalidProjectException("Attribute 'redcapUrl' is required.");
    }
    
    if (!fp.hasToken()) {
      throw new InvalidProjectException("Attribute 'token' is required.");
    }
    
    // Check if this project exists
    final Optional<RedmatchProject> project = dao.getRedmatchProject(fp.getId());
    if (project.isPresent()) {
      throw new ProjectAlreadyExistsException("The Redmatch project " + fp.getId() 
        + " already exists.");
    }
    
    // Populate Redmatch project with metadata from REDCap
    fp.setMetadata(getMetadataFromRedcap(fp.getRedcapUrl(), fp.getToken(), fp.getReportId()));
    
    // Compile rules document, validate and generate mappings
    processRedmatchProject(fp);
    
    // Save project
    dao.saveRedmatchProject(fp);
    
    return new OperationResponse(fp.getId(), RegistrationStatus.CREATED);
  }
  
  /**
   * Returns a Redmatch project given its id or throws an exception if such a
   * project does not exist.
   * 
   * @param id The local id of the Redmatch project.
   * @return The {@link RedmatchProject} instance.
   * @throws ProjectNotFoundException If the Redmatch {@link RedmatchProject} with the specified 
   *     id does not exist.
   */
  public RedmatchProject resolveRedmatchProject(String id) {
    final Optional<RedmatchProject> project = dao.getRedmatchProject(id);
    if (!project.isPresent()) {
      throw new ProjectNotFoundException("The Redmatch project " + id + " was not found");
    } else {
      return project.get();
    }
  }
  
  /**
   * Updates the specified Redmatch project. The following fields might be updated:
   * 
   * <ul>
   *   <li>token, because the user might need to change the API token</li>
   *   <li>name, because this is a user-defined name</li>
   *   <li>rulesDocument, because the transformation rules need to be defined by the user</li>
   *   <li>mappings, because the mappings need to be defined by the user</li>
   * </ul>
   * 
   * @param newProject The project that contains the updates.
   * @return The result of the update.
   */
  @Transactional
  public OperationResponse updateRedmatchProject(RedmatchProject newProject) {
    // Try to resolve existing project
    final RedmatchProject existingProject = resolveRedmatchProject(newProject.getId());
    if (existingProject == null) {
      throw new ProjectNotFoundException("Redmatch project " + newProject.getId() 
        + " was not found.");
    } else {
      if (newProject.hasToken()) {
        existingProject.setToken(newProject.getToken());
      }
      
      if (newProject.hasName()) {
        existingProject.setName(newProject.getName());
      }
      
      if (newProject.hasRulesDocument()) {
        existingProject.setRulesDocument(newProject.getRulesDocument());
        processRedmatchProject(existingProject);
      }
      
      if (newProject.hasMappings()) {
        mergeMappings(newProject.getMappings(), existingProject.getMappings());
      }
      
      dao.saveRedmatchProject(existingProject);
      return new OperationResponse(existingProject.getId(), RegistrationStatus.UPDATED);
    }
  }

  /**
   * Returns all projects in Redmatch.
   * 
   * @param elems The list of names of top-level attributes to keep.
   * 
   * @return All Redmatch projects in the system.
   */
  public List<RedmatchProject> getRedmatchProjects(List<String> elems) {
    final List<RedmatchProject> res = new ArrayList<>();
    for (RedmatchProject s : dao.findRedmatchProjects()) {
      res.add(ReflectionUtils.filterElems(s.getClass(), s, elems));
    }
    return res;
  }

  /**
   * Returns a project's mappings rendered as an Excel spreadsheat.
   * 
   * @param projectId The id of the Redmatch project.
   * @return The byte representation of the Excel spreadsheat.
   */
  public byte[] getMappingsExcel(String projectId) {
    final List<Mapping> mappings = getMappings(projectId);

    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      excelExporter.exportStudy(projectId, mappings, baos);
      final HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Disposition", "attachment; filename=\"study_" + projectId + ".xls\"");

      final byte[] res = baos.toByteArray();
      return res;
    } catch (IOException e) {
      throw new RedmatchException("There was an I/O problem exporting the mappings to Excel.", e);
    }
  }

  /**
   * Returns the current mappings for a project.
   * 
   * @param projectId The project id.
   * @return The current mappings.
   */
  public List<Mapping> getMappings(String projectId) {
    return resolveRedmatchProject(projectId).getMappings();
  }
  
  /**
   * Updates a project's mappings.
   * 
   * @param mappings The new mappings.
   * @return The result of the update.
   */
  @Transactional
  public OperationResponse updateMappings(String projectId, List<Mapping> newMappings) {
    log.info("Updating mappings.");
    final RedmatchProject project = resolveRedmatchProject(projectId);
    log.info("Project " + projectId + " has " + project.getMappings().size() + " mappings.");
    log.info("Received " + newMappings.size() + " new mappings.");
    
    final List<Mapping> oldMappings = project.getMappings();
    
    if (newMappings.size() != oldMappings.size()) {
      throw new InvalidMappingsException("Received " + newMappings.size() 
        + " mappings but expected " + oldMappings.size());
    }
    
    for (int i = 0; i < oldMappings.size(); i++) {
      final Mapping oldMapping = oldMappings.get(i);
      final Mapping newMapping = newMappings.get(i);
      
      if (!oldMapping.getRedcapFieldId().equals(newMapping.getRedcapFieldId())) {
        throw new InvalidMappingsException("There was a mismatched in the uploded mappings. "
            + "Expected " + oldMapping.getRedcapFieldId() + " but got " 
            + newMapping.getRedcapFieldId());
      }
    }
    
    project.replaceMappings(newMappings);
    dao.saveRedmatchProject(project);
    return new OperationResponse(project.getId(), RegistrationStatus.UPDATED);
  }

  /**
   * Imports the mappings from an Excel spreadsheat.
   * 
   * @param projectId The id of the Redmatch project.
   * @param bytes The bytes that represent the Excel spreadsheat.
   * 
   * @return The result of the update.
   */
  @Transactional
  public OperationResponse importMappingsExcel(String projectId, byte[] bytes) {
    log.info("Importing mappings from Excel.");
    final RedmatchProject project = resolveRedmatchProject(projectId);
    log.info("Project " + projectId + " has " + project.getMappings().size() + " mappings.");
    try {
      final Workbook wb = new HSSFWorkbook(new ByteArrayInputStream(bytes), false);
      final List<Mapping> newMappings = excelImporter.importMappings(wb);
      log.info("Imported " + newMappings.size() + " from Excel.");
      
      return updateMappings(projectId, newMappings);
    } catch (IOException e) {
      throw new RedmatchException("There was an I/O problem importing the mappings from Excel.", e);
    }
  }
  
  /**
   * Updates the transformation rules document.
   * 
   * @param projectId The id of the Redmatch project.
   * @param rulesDocument The new transformation rules document.
   * @return The result of the update.
   */
  @Transactional
  public OperationResponse updateRulesDocument(String projectId, String rulesDocument) {
    final RedmatchProject project = resolveRedmatchProject(projectId);
    final RedmatchProject newProject = new RedmatchProject(project.getReportId(), 
        project.getRedcapUrl());
    newProject.setRulesDocument(rulesDocument);
    return updateRedmatchProject(newProject);
  }

  /**
   * Returns the rules document for a Redmatch project.
   * 
   * @param projectId The Redmatch project id.
   * @return The rules document.
   */
  public String getRulesDocument(String projectId) {
    return resolveRedmatchProject(projectId).getRulesDocument();
  }
  
  /**
   * Transforms a Redmatch project using the current rules and mappings and returns a FHIR bundle
   * with the generated resources.
   * 
   * @param projectId The id of the Redmatch project to transform.
   */
  public Bundle transformProject(String projectId) {
    log.info("Transforming Redmatch project " + projectId + ".");
    final RedmatchProject fcp = resolveRedmatchProject(projectId);
    if (fcp.hasErrors()) {
      throw new CompilerException(getRuleValidationErrorMessage(fcp.getIssues()));
    }
    
    log.info("Getting data from REDCap.");
    final List<Row> rows = redcapImporter.getReport(fcp.getRedcapUrl(), fcp.getToken(), 
        fcp.getReportId());
    log.info("Retrieved " + rows.size() + " records.");
    
    final Metadata metadata = fcp.getMetadata();
    final String rulesString = fcp.getRulesDocument();
    final Document rulesDocument;
    if (rulesString == null || rulesString.isEmpty()) {
      rulesDocument = null;
    } else {
      rulesDocument = compiler.compile(rulesString, metadata);
    }
    final List<Annotation> errors = compiler.getErrorMessages();
    if (!errors.isEmpty()) {
      throw new RedmatchException("There were errors compiling the rules but the project had no "
          + "issues. This should not happen!");
    }
    
    final List<Mapping> mappings = fcp.getMappings();
    
    log.info("Exporting data to FHIR.");
    final Bundle res = fhirExporter.createClinicalBundle(metadata, rulesDocument, mappings, rows);
    if (res.hasEntry()) {
      log.info("Generated bundle with " + res.getEntry().size() + " entries.");
    } else {
      log.info("Generated bundle is empty.");
    }
    return res;
  }
  
  /**
   * Deletes a Redmatch project.
   * 
   * @param id The Redmatch project id.
   */
  @Transactional
  public void deleteRedmatchProject(String id) {
    final Optional<RedmatchProject> project = dao.getRedmatchProject(id);
    if (project.isPresent()) {
      dao.deleteRedmatchProject(id);
    } else {
      throw new ProjectNotFoundException("The Redmatch project " + id + " was not found");
    }
    
  }

  /**
   * Updated the REDCap data in a Redmatch project.
   * 
   * @param redmatchProjectId
   *          The id of the Redmatch project.
   */
  @Transactional
  public void refreshRedcapMetadata(String redmatchProjectId) {
    // We need to get the Redmatch project
    final Optional<RedmatchProject> fcp = dao.getRedmatchProject(redmatchProjectId);
    if (!fcp.isPresent()) {
      throw new ProjectNotFoundException("Redmatch project with id " + redmatchProjectId 
          + " was not found.");
    }
    
    final RedmatchProject fp = fcp.get();
    processRedmatchProject(fp);
    dao.saveRedmatchProject(fp);
  }
  
  /**
   * Gets the metadata from REDCap.
   * 
   * @param url The REDCap API URL.
   * @param token The REDCap API token.
   * @param reportId The id of the report that determines which fields are retrieved.
   * @return
   */
  private Metadata getMetadataFromRedcap(String url, String token, String reportId) {
    // We need to get the data from a report first, to know which metadata fields we need. REDCap
    // does not allow accessing the report definition.
    log.debug("Getting report data to find required fields");
    final Set<String> fieldIds = new HashSet<>();
    final List<Row> report = redcapImporter.getReport(url, token, reportId);
    fieldIds.addAll(extractFields(report.get(0).getData().keySet()));
    
    // Get the metadata and filter it based on the field names
    log.debug("Getting metadata for required fields");
    final Metadata metadata = redcapImporter.getMetadata(url, token, fieldIds);
    return metadata;
  }
  
  /**
   * Replaces fields of the from ___x with the simple name of the field.
   * 
   * @param keySet
   * @return
   */
  private Set<String> extractFields(Set<String> keySet) {
    final Set<String> res = new HashSet<>();
    
    for (String key : keySet) {
      res.add(key.replaceAll("___.*", ""));
    }
    
    return res;
  }
  
  /**
   * Processes a Redmatch project. Does the following:
   * 
   * <ol>
   *   <li>Compiles the rules document, validates the compatibility of expressions and field types
   *   and checks if rules reference non-existent fields. Any issues are added to the project's 
   *   validation errors.</li>
   *   <li>Generates mappings from the rules.</li>
   * </ol>
   * 
   * @param rmp
   */
  private void processRedmatchProject(RedmatchProject rmp) {
    final Metadata metadata = rmp.getMetadata();
    
    // Compile rules document and add validation errors.
    final Document doc = compiler.compile(rmp.getRulesDocument(), metadata);
    rmp.setIssues(compiler.getErrorMessages());
    
    // The document might be empty, depending on the type of errors
    if (doc == null) {
      return;
    }
    
    // Generate mappings from rules
    final List<Mapping> tgtMappings = redcapImporter.generateMappings(
        metadata, 
        doc.getReferencedFields(metadata), 
        redcapImporter.getReport(rmp.getRedcapUrl(), rmp.getToken(), rmp.getReportId())
    );
    
    // Merge mappings
    final List<Mapping> srcMappings = rmp.getMappings();
    mergeMappings(srcMappings, tgtMappings);
    
    rmp.replaceMappings(tgtMappings);
  }
  
  /**
   * Merges the <i>srcMappings</i> collection into the <i>tgtMappings</i> collection. Assumes 
   * <i>tgtMappings</i> are the valid ones, i.e., the ones that have been generated from the most 
   * recent version of  a rules document and REDCap metadata. Any existing mapped values that are 
   * not in <i>tgtMappings</i> are copied over. Existing mappings in <i>tgtMappings</i> are not 
   * overwritten and any additional mappings in <i>srcMappings</i> that are not present in 
   * <i>tgtMappings</i> are discarded. Modifications are made in place in <i>tgtMappings</i>.
   * 
   * @param srcMappings The mappings to merge.
   * @param tgtMappings The target mappings where the source mappings are merged. These are 
   * modified in place.
   */
  private void mergeMappings(List<Mapping> srcMappings, List<Mapping> tgtMappings) {
    // Optimisation
    if (srcMappings.isEmpty()) {
      return;
    }
    
    for (Mapping mapping : tgtMappings) {
      // Look for a match in the old mappings - a match is a mapping with the same fieldId and the
      // same text
      for (Mapping oldMapping : srcMappings) {
        if (mapping.getRedcapFieldId().equals(oldMapping.getRedcapFieldId()) 
            && ((mapping.getText() == null && oldMapping.getText() == null) 
                || (mapping.getText() != null && mapping.getText().equals(oldMapping.getText())))) {
          if (oldMapping.hasTargetSystem() && !mapping.hasTargetSystem()) {
            // Copy targets over
            mapping.setTargetSystem(oldMapping.getTargetSystem());
            mapping.setTargetCode(oldMapping.getTargetCode());
            mapping.setTargetDisplay(oldMapping.getTargetDisplay());
            mapping.setValueSetUrl(oldMapping.getValueSetUrl());
            mapping.setValueSetName(oldMapping.getValueSetName());
          }
        }
      }
    }
  }
  
  /**
   * Creates a single error message from a list of {@link Annotation}s.
   * 
   * @param errors List of errors.
   * @return A string with all the error messages.
   */
  private String getRuleValidationErrorMessage(List<Annotation> errors) {
    StringBuilder sb = new StringBuilder();
    sb.append("The following errors were detected:\n");
    for (Annotation error : errors) {
      if (error.getAnnotationType().equals(AnnotationType.ERROR)) {
        sb.append(error);
        sb.append("\n");
      }
    }
    return sb.toString();
  }

}
