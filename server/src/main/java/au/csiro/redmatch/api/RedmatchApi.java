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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import au.csiro.redmatch.model.*;
import au.csiro.redmatch.util.HashUtils;
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
import au.csiro.redmatch.model.OperationResponse.RegistrationStatus;
import au.csiro.redmatch.model.grammar.redmatch.Document;
import au.csiro.redmatch.persistence.RedmatchDao;
import au.csiro.redmatch.util.ReflectionUtils;
import ca.uhn.fhir.parser.DataFormatException;

import javax.validation.constraints.NotNull;

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
   * @param name The name of the project.
   * @param reportId The id of the report in REDCap.
   * @param redcapUrl The URL of the REDCap API.
   * @param token The API token.
   * @return {@link OperationResponse} indicating if the project was registered or already existed.
   */
  @Transactional
  public OperationResponse createRedmatchProject(
          @NotNull String name,
          @NotNull String reportId,
          @NotNull String redcapUrl,
          @NotNull String token) {
    log.info("Creating Redmatch project " + name);
    
    // Check if this project exists
    String projectId = HashUtils.shortHash(redcapUrl) + reportId;
    if (dao.existsById(projectId)) {
      throw new ProjectAlreadyExistsException("The Redmatch project " + projectId + " already exists.");
    }
    
    // Populate Redmatch project with metadata from REDCap
    RedmatchProject project = new RedmatchProject(reportId, redcapUrl, token, name);
    addMetadataFromRedcap(project);
    
    // Compile rules document, validate and generate mappings
    processRedmatchProject(project);
    
    // Save project
    dao.save(project);
    
    return new OperationResponse(project.getId(), RegistrationStatus.CREATED);
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

    final Optional<RedmatchProject> project = dao.findById(id);
    if (project.isPresent()) {
      RedmatchProject rmp = project.get();
      if (!includeInactiveMappings) {
        List<Mapping> filteredMappings = rmp.getMappings().stream().filter(
                m -> !m.isInactive()).collect(Collectors.toList());
        rmp.replaceMappings(filteredMappings);
      }
      return rmp;
    } else {
      throw new ProjectNotFoundException("The Redmatch project " + id + " was not found");
    }
  }
  
  /**
   * Updates the specified Redmatch project. The following fields are mandatory:
   * <ul>
   *   <li>name</li>
   *   <li>reportId</li>
   *   <li>redcapUrl</li>
   *   <li>token</li>
   * </ul>
   *
   * The following fields should never change and should therefore match the ones on the existing project:
   *
   * <ul>
   *   <li>reportId</li>
   *   <li>redcapUrl</li>
   * </ul>
   *
   * Note that all mappings will be replaced so the user should ensure that all inactive mappings are present or
   * otherwise they will be lost.
   * 
   * @param newProject The project that contains the updates.
   * @return The result of the update.
   */
  @Transactional
  public OperationResponse updateRedmatchProject(RedmatchProject newProject) {
    log.info("Updating Redmatch project " + newProject.getId());
    final RedmatchProject existingProject = resolveRedmatchProject(newProject.getId(), true);
    if (existingProject == null) {
      throw new ProjectNotFoundException("Redmatch project " + newProject.getId()
        + " was not found.");
    }

    if (!newProject.hasName()) {
      throw new InvalidProjectException("The attribute 'name' is mandatory.");
    }
    if(!newProject.hasReportId()) {
      throw new InvalidProjectException("The attribute 'reportId' is mandatory.");
    } else if (!newProject.getReportId().equals(existingProject.getReportId())) {
      throw new InvalidProjectException("The reportId " + newProject.getReportId() + " in the new project does not " +
        "match the reportId " + existingProject.getReportId() + " in the existing project.");
    }
    if(!newProject.hasRedcapUrl()) {
      throw new InvalidProjectException("The attribute 'redcapUrl' is mandatory.");
    } else if (!newProject.getRedcapUrl().equals(existingProject.getRedcapUrl())) {
      throw new InvalidProjectException("The redcapUrl " + newProject.getRedcapUrl() + " in the new project does not " +
        "match the redcapUrl " + existingProject.getRedcapUrl() + " in the existing project.");
    }
    if (!newProject.hasToken()) {
      throw new InvalidProjectException("The attribute 'token' is mandatory.");
    }
      
    processRedmatchProject(newProject);
    RedmatchProject save = dao.save(newProject);
    return new OperationResponse(save.getId(), RegistrationStatus.UPDATED);
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
    for (RedmatchProject s : dao.findAll()) {
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
      return baos.toByteArray();
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
   * @param projectId The Redmatch project id.
   * @param newMappings The new mappings.
   * @return The result of the update.
   */
  @Transactional
  public OperationResponse updateMappings(String projectId, List<Mapping> newMappings) {
    log.info("Updating mappings");
    final RedmatchProject project = resolveRedmatchProject(projectId, false);
    project.setMappings(newMappings);
    return updateRedmatchProject(project);
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
    final RedmatchProject project = resolveRedmatchProject(projectId, true);
    project.setRulesDocument(rulesDocument);
    return updateRedmatchProject(project);
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
   * @throws IOException If an I/O errors occurs.
   * @throws DataFormatException If there is a parsing problem with the HAPI libraries.
   */
  public Parameters transformProject(String projectId) throws DataFormatException, IOException {
    log.info("Transforming Redmatch project " + projectId);
    final RedmatchProject project = resolveRedmatchProject(projectId, false);
    if (project.hasErrors()) {
      throw new CompilerException(getRuleValidationErrorMessage(project.getIssues()));
    }
    
    if (!project.isMappingsComplete()) {
      throw new NoMappingFoundException("Project has missing mappings. Please make sure all "
          + "mappings are complete before attempting to transform the project.");
    }
    
    log.info("Getting data from REDCap.");
    final List<Row> rows = redcapImporter.getReport(project.getRedcapUrl(), project.getToken(),
        project.getReportId());
    log.info("Retrieved " + rows.size() + " records.");

    final Document rulesDocument = compiler.compile(project);
    
    log.info("Exporting data to FHIR.");
    Map<String, String> map = fhirExporter.saveResourcesToFolder(project, rulesDocument, rows);
    
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
   * @throws IOException If an I/O error occurs.
   */
  public byte[] downloadExportedFiles(String projectId) throws IOException {
    // Read ND-JSON files and save to ZIP file
    File folder = new File(targetFolderName, projectId);
    log.info("Downloading ND-JSON files from " + folder);
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      try (ZipOutputStream zos = new ZipOutputStream(baos)) {
        try (Stream<Path> walk = Files.walk(folder.toPath())) {
          walk.map(Path::toAbsolutePath)
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
    final Optional<RedmatchProject> project = dao.findById(id);
    if (project.isPresent()) {
      dao.deleteById(id);
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
    final Optional<RedmatchProject> fcp = dao.findById(redmatchProjectId);
    if (fcp.isEmpty()) {
      throw new ProjectNotFoundException("Redmatch project with id " + redmatchProjectId 
          + " was not found.");
    }
    
    final RedmatchProject project = fcp.get();
    
    // Update metadata
    log.info("Getting new metadata from REDCap");
    project.deleteAllFields();
    addMetadataFromRedcap(project);
    processRedmatchProject(project);
    RedmatchProject res = dao.save(project);

    log.info("Finished updating metadata");
    return new OperationResponse(res.getId(), RegistrationStatus.UPDATED);
  }
  
  /**
   * Gets the metadata from REDCap.
   * 
   * @param project The Redmatch project. Should have the <i>url</i>, <i>token</i> and <i>reportId</i> attributes
   *                populated.
   */
  private void addMetadataFromRedcap(RedmatchProject project) {
    // We need to get the data from a report first, to know which metadata fields we need. REDCap
    // does not allow accessing the report definition.
    log.debug("Getting report data to find required fields");
    final List<Row> report =
            redcapImporter.getReport(project.getRedcapUrl(), project.getToken(), project.getReportId());
    final Set<String> fieldIds = new HashSet<>(extractFields(report.get(0).getData().keySet()));
    
    // Get the metadata and filter it based on the field names
    log.debug("Getting metadata for required fields");
    redcapImporter.addMetadata(fieldIds, project);
  }
  
  /**
   * Replaces fields of the from ___x with the simple name of the field.
   * 
   * @param keySet The set of field names.
   * @return A set with all the simple field names.
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
   * @param rmp The Redmatch project to process.
   */
  private void processRedmatchProject(RedmatchProject rmp) {

    // Sort mappings here so order is always the same
    if (rmp.hasMappings()) {
      Collections.sort(rmp.getMappings());
    }

    // Compile rules document and add validation errors.
    final Document doc = compiler.compile(rmp);
    
    // If the document is empty or has any errors then we inactivate all current mappings
    if (doc == null || rmp.hasErrors()) {
      for (Mapping m : rmp.getMappings()) {
        m.setInactive(true);
      }
      return;
    }
    
    // Generate mappings from rules
    final List<Mapping> ruleMappings = generateMappings(
        rmp.getFields(),
        doc.getReferencedFields(rmp),
        redcapImporter.getReport(rmp.getRedcapUrl(), rmp.getToken(), rmp.getReportId())
    );
    
    // Merge mappings
    final List<Mapping> newMappings = rmp.getMappings();
    final List<Mapping> mergedMappings = mergeMappings(ruleMappings, newMappings);
    
    rmp.replaceMappings(mergedMappings);
  }

  /**
   * Generates the mappings from the metadata.
   *
   * @param fields The fields in a Redmatch project, i.e., its metadata.
   * @param fieldIds The map of the ids of the fields defined in the transformation rules and a
   * boolean value that indicates if they are required. A field requires mapping when it is part of
   * the "resource" section of the rule and uses the CONCEPT, CONCEPT_SELECTED or CODE_SELECTED
   * keyword.
   * @param report The rows of the report. Can be null if there are not CONCEPT mappings to plain
   * text fields.
   *
   * @return The mappings for the fields defined in the rules. If the fields in the rules reference
   *         fields that do not exist in REDCap then these will be ignored.
   */
  public List<Mapping> generateMappings(List<Field> fields, Map<String, Boolean> fieldIds,
                                        List<Row> report) {
    log.info("Generating mappings");
    if (fieldIds.isEmpty()) {
      return Collections.emptyList(); // Optimisation
    }

    final List<Mapping> res = new ArrayList<>();
    log.debug("Processing " + fields.size() + " fields.");

    Set<String> fieldsWithTextToMap = new HashSet<>();
    for (Field field : fields) {
      String redcapFieldId = field.getFieldId();
      log.debug("Processing field " + redcapFieldId);
      if (fieldIds.containsKey(redcapFieldId) && fieldIds.get(redcapFieldId)) {
        log.debug("Creating mappings for field " + field);
        String label = field.getFieldLabel();
        Field.FieldType fieldType = field.getFieldType();

        if (Field.FieldType.TEXT.equals(fieldType)) {
          // Need to create a mapping for each data element
          fieldsWithTextToMap.add(redcapFieldId);
        } else {
          Mapping mapping = new Mapping();
          mapping.setRedcapFieldId(redcapFieldId);
          mapping.setRedcapFieldType(fieldType.toString());
          mapping.setRedcapLabel(label);
          res.add(mapping);
        }
      }
    }

    if (!fieldsWithTextToMap.isEmpty()) {
      if (report == null) {
        throw new RedmatchException("There are text mappings in fields " + fieldsWithTextToMap
                + " but no report was provided.");
      }

      final Map<String, Set<String>> map = new HashMap<>();
      for (String field : fieldsWithTextToMap) {
        map.put(field, new HashSet<>());
      }

      // Get data and index
      for (Row row : report) {
        final Map<String, String> data = row.getData();
        for (String field : fieldsWithTextToMap) {
          String s = data.get(field);
          if (s != null && !s.isBlank()) {
            map.get(field).add(s);
          }
        }
      }

      for (Field field : fields) {
        String redcapFieldId = field.getFieldId();
        log.debug("Processing field " + redcapFieldId);
        if (fieldIds.containsKey(redcapFieldId) && fieldIds.get(redcapFieldId)) {
          log.debug("Creating data mappings for field " + field);
          String label = field.getFieldLabel();
          Field.FieldType fieldType = field.getFieldType();

          if (Field.FieldType.TEXT.equals(fieldType)) {
            for (String text : map.get(redcapFieldId)) {
              // Need to create a mapping for each data element
              Mapping mapping = new Mapping();
              mapping.setRedcapFieldId(redcapFieldId);
              mapping.setRedcapFieldType(fieldType.toString());
              mapping.setRedcapLabel(label);
              mapping.setText(text);
              res.add(mapping);
            }
          }
        }
      }
    }

    Collections.sort(res);
    return res;
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
      if (index == -1) {
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
