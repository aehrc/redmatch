/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.UrlType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import au.csiro.redmatch.compiler.RedmatchCompiler;
import au.csiro.redmatch.exceptions.RedmatchException;
import au.csiro.redmatch.exporter.ExcelExporter;
import au.csiro.redmatch.exporter.FhirExporter;
import au.csiro.redmatch.exporter.NoMappingFoundException;
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
import ca.uhn.fhir.parser.DataFormatException;

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

  @Value("${redmatch.targetFolder}")
  private String targetFolderName;
  
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
    log.info("Creating Redmatch project");
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
   * @param includeInactiveMappings If false, inactive mappings are excluded.
   * @return The {@link RedmatchProject} instance.
   * @throws ProjectNotFoundException If the Redmatch {@link RedmatchProject} with the specified 
   *     id does not exist.
   */
  public RedmatchProject resolveRedmatchProject(String id, boolean includeInactiveMappings) {
    log.info("Resolving Redmatch project " + id);
    final Optional<RedmatchProject> project = dao.getRedmatchProject(id);
    if (!project.isPresent()) {
      throw new ProjectNotFoundException("The Redmatch project " + id + " was not found");
    } else {
      RedmatchProject rmp = project.get();
      if (includeInactiveMappings) {
        return rmp;
      } else {
        List<Mapping> filteredMappings = rmp.getMappings().stream().filter(
            m -> !m.isInactive()).collect(Collectors.toList());
        rmp.replaceMappings(filteredMappings);
        return rmp;
      }
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
    log.info("Updating Redmatch project " + newProject.getId());
    // Try to resolve existing project
    final RedmatchProject existingProject = resolveRedmatchProject(newProject.getId(), true);
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
      }
      
      if (newProject.hasMappings()) {
        List<Mapping> combinedMappings = mergeMappings(existingProject.getMappings(), 
            newProject.getMappings());
        existingProject.replaceMappings(combinedMappings);
      }
      
      processRedmatchProject(existingProject);
      
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
    log.info("Looking for all Redmatch projects");
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
    log.info("Retrieving mappings in Excel format for project " + projectId);
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
    log.info("Retrieving mappings for project " + projectId);
    return resolveRedmatchProject(projectId, false).getMappings();
  }
  
  /**
   * Updates a project's mappings.
   * 
   * @param mappings The new mappings.
   * @return The result of the update.
   */
  @Transactional
  public OperationResponse updateMappings(String projectId, List<Mapping> newMappings) {
    log.info("Updating mappings");
    final RedmatchProject project = resolveRedmatchProject(projectId, false);
    final RedmatchProject newProject = new RedmatchProject(project.getReportId(), 
        project.getRedcapUrl());
    newProject.addMappings(newMappings);
    return updateRedmatchProject(newProject);
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
    final RedmatchProject project = resolveRedmatchProject(projectId, false);
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
    log.info("Updating rules document");
    final RedmatchProject project = resolveRedmatchProject(projectId, false);
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
    log.info("Looking for rules document for project " + projectId);
    return resolveRedmatchProject(projectId, false).getRulesDocument();
  }
  
  /**
   * Transforms a Redmatch project using the current rules and mappings, stores the result in the
   * target folder using ND-JSON and returns an operation outcome.
   * 
   * @param projectId The id of the Redmatch project to transform.
   * @throws IOException 
   * @throws DataFormatException 
   */
  public Parameters transformProject(String projectId) throws DataFormatException, IOException {
    log.info("Transforming Redmatch project " + projectId);
    final RedmatchProject fcp = resolveRedmatchProject(projectId, false);
    if (fcp.hasErrors()) {
      throw new CompilerException(getRuleValidationErrorMessage(fcp.getIssues()));
    }
    
    if (!fcp.isMappingsComplete()) {
      throw new NoMappingFoundException("Project has missing mappings. Please make sure all "
          + "mappings are complete before attempting to transform the project.");
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
    Map<String, String> map = fhirExporter.saveResourcesToFolder(projectId, metadata, rulesDocument,
        mappings, rows);
    
    Parameters res = new Parameters();
    
    for (String resourceType : map.keySet()) {
      ParametersParameterComponent ppc = res.addParameter().setName("source");
      ppc.addPart()
        .setName("resourceType")
        .setValue(new CodeType(resourceType));
      ppc.addPart()
        .setName("url")
        .setValue(new UrlType("file://" + map.get(resourceType)));
    }
    
    return res;
  }
  
  /**
   * Downloads the ND-JSON files in the target folder as a ZIP file.
   * 
   * @return The ND-JSON files as a ZIP file.
   * @throws IOException 
   */
  public byte[] downloadExportedFiles(String projectId) throws IOException {
    // Read ND-JSON files and save to ZIP file
    File folder = new File(targetFolderName, projectId);
    log.info("Downloading ND-JSON files from " + folder);
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      try (ZipOutputStream zos = new ZipOutputStream(baos)) {
        try (Stream<Path> walk = Files.walk(folder.toPath())) {
          walk.map(x -> x.toAbsolutePath())
            .filter(f -> {
              log.info("Processing file " + f);
              return f.toString().endsWith(".ndjson");
            })
            .collect(Collectors.toList())
            .forEach(x -> {
              log.info("Adding file " + x + " to zip.");
              try {
                ZipEntry zipEntry = new ZipEntry(x.getFileName().toString());
                zos.putNextEntry(zipEntry);
                ByteArrayInputStream bais = new ByteArrayInputStream(Files.readAllBytes(x));
  
                byte[] buffer = new byte[1024];
                int len;
                while ((len = bais.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
        }
      }
      return baos.toByteArray();
    }
  }
  
  /**
   * Deletes a Redmatch project.
   * 
   * @param id The Redmatch project id.
   */
  @Transactional
  public void deleteRedmatchProject(String id) {
    log.info("Deleting Redmatch project " + id);
    final Optional<RedmatchProject> project = dao.getRedmatchProject(id);
    if (project.isPresent()) {
      dao.deleteRedmatchProject(id);
    } else {
      throw new ProjectNotFoundException("The Redmatch project " + id + " was not found");
    }
    
  }

  /**
   * Updates the REDCap metadata in a Redmatch project.
   * 
   * @param redmatchProjectId
   *          The id of the Redmatch project.
   * @return The result of the update.
   */
  @Transactional
  public OperationResponse refreshRedcapMetadata(String redmatchProjectId) {
    log.info("Refreshing REDCap metadata for project " + redmatchProjectId);
    // We need to get the Redmatch project
    final Optional<RedmatchProject> fcp = dao.getRedmatchProject(redmatchProjectId);
    if (!fcp.isPresent()) {
      throw new ProjectNotFoundException("Redmatch project with id " + redmatchProjectId 
          + " was not found.");
    }
    
    final RedmatchProject project = fcp.get();
    
    // Update metadata
    log.info("Getting new metadata from REDCap");
    project.setMetadata(getMetadataFromRedcap(project.getRedcapUrl(), project.getToken(), 
        project.getReportId()));
    
    processRedmatchProject(project);
    dao.saveRedmatchProject(project);
    
    log.info("Finished updating metadata");
    return new OperationResponse(project.getId(), RegistrationStatus.UPDATED);
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
    final List<Mapping> ruleMappings = redcapImporter.generateMappings(
        metadata, 
        doc.getReferencedFields(metadata), 
        redcapImporter.getReport(rmp.getRedcapUrl(), rmp.getToken(), rmp.getReportId())
    );
    
    // Merge mappings
    final List<Mapping> newMappings = rmp.getMappings();
    final List<Mapping> mergedMappings = mergeMappings(ruleMappings, newMappings);
    
    rmp.replaceMappings(mergedMappings);
  }
  
  /**
   * <p>Merges <i>newMappings</i> into <i>existingMappings</i>. New mappings can contain references 
   * to mappings that no longer exist, because the rules document changed, for example.</p>
   * 
   * <p>Existing mappings are empty mappings derived from the transformation rules or mappings that 
   * have been previously supplied by the user. New mappings are typically supplied by the user. No
   * mapping should ever be removed. Mappings that are not in </p>existingMappings</i> should be
   * inactivated. When a mapping is available in both lists the value should be taken from 
   * <i>newMappings</i>.
   * 
   * @param existingMappings The existing mappings.
   * @param newMappings The new mappings.
   * 
   * @return The merged list.
   */
  private List<Mapping> mergeMappings(List<Mapping> existingMappings, List<Mapping> newMappings) {
    log.info("Merging " + newMappings.size() + " new mappings into " + existingMappings.size() + 
        " existing mappings");
    
    final List<Mapping> res = new ArrayList<>();
    for (Mapping existingMapping : existingMappings) {
      // See if mapping is in both lists
      int index = newMappings.indexOf(existingMapping);
      if (index != -1) {
        Mapping newMapping = newMappings.get(index);
        newMapping.setInactive(existingMapping.isInactive());
        log.debug("New mapping " + newMapping + " was added.");
        res.add(newMapping);
      } else {
        log.debug("Existing mapping " + existingMapping + " was added.");
        res.add(existingMapping);
      }
    }
    
    for (Mapping newMapping : newMappings) {
      // See if mapping is in both lists
      int index = existingMappings.indexOf(newMapping);
      if (index != -1) {
        // Do nothing - if in both then it was added before
      } else {
        // Inactive because not in existing mappings
        newMapping.setInactive(true);
        log.debug("New mapping " + newMapping + " was added.");
        res.add(newMapping);
      }
    }
    Collections.sort(res);
    return res;
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
