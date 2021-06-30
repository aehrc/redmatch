/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.web;

import au.csiro.redmatch.util.RawJson;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.beans.BeanProperty;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.gson.Gson;

import au.csiro.redmatch.api.RedmatchApi;
import au.csiro.redmatch.api.InvalidMappingsException;
import au.csiro.redmatch.api.ProjectAlreadyExistsException;
import au.csiro.redmatch.api.ProjectNotFoundException;
import au.csiro.redmatch.client.HttpException;
import au.csiro.redmatch.exporter.NoMappingFoundException;
import au.csiro.redmatch.exporter.RuleApplicationException;
import au.csiro.redmatch.importer.CompilerException;
import au.csiro.redmatch.model.RedmatchProject;
import au.csiro.redmatch.model.Mapping;
import au.csiro.redmatch.model.OperationResponse;
import au.csiro.redmatch.util.WebUtils;

/**
 * The main controller.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@CrossOrigin(exposedHeaders = "location")
@RestController
public class ApiController {

  /** Logger. */
  private static final Log log = LogFactory.getLog(ApiController.class);

  @Autowired @Qualifier("api")
  private RedmatchApi api;
  
  @Autowired
  private Gson gson;

  @Value("${ontoserver.url}")
  private String ontoserverUrl;

  @Value("${ui.keycloak.url}")
  private String keycloakUrl;

  @Value("${ui.keycloak.realm}")
  private String keycloakRealm;

  @Value("${ui.keycloak.clientId}")
  private String keycloakClientId;

  @Autowired
  private Environment environment;
  
  @Bean
  public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
    List<MediaType> mediaTypes = new ArrayList<>();
    mediaTypes.addAll(jsonConverter.getSupportedMediaTypes());
    mediaTypes.add(new MediaType("application", "javascript"));
    jsonConverter.setSupportedMediaTypes(mediaTypes);
    return jsonConverter;
  }

  /**
   * Returns all the Redmatch projects in the system.
   * 
   * @param elements The elements of the Redmatch project to return.
   * @return The response entity.
   */
  @ApiOperation(value = "Returns information about a REDCap project registered in Redmatch.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project", method = RequestMethod.GET, 
      produces = "application/json")
  public ResponseEntity<List<RedmatchProject>> getRedmatchProjects(
      @RequestParam(value = "_elements", required = false) String elements) {
    final List<String> elems = parseElements(elements);
    final List<RedmatchProject> projects = api.getRedmatchProjects(elems);
    return new ResponseEntity<>(projects, HttpStatus.OK);
  }

  /**
   * Registers a Redmatch project.
   *
   * @param project The Redmatch project to register.
   * @return The response entity.
   */
  @ApiOperation(value = "Registers a project with Redmatch.")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "The Redmatch project was created successfully."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") 
  })
  @RequestMapping(value = "/project", method = RequestMethod.POST, 
      consumes = "application/json", produces = "application/json")
  public ResponseEntity<RedmatchProject> createRedmatchProject(
      @RequestBody RedmatchProject project,
      UriComponentsBuilder b) {
    
    final OperationResponse rr = api.createRedmatchProject(project.getName(), project.getReportId(),
            project.getRedcapUrl(), project.getToken());
    final RedmatchProject res = api.resolveRedmatchProject(rr.getProjectId(), false);

    final UriComponents uriComponents = b.path("/project/{id}").buildAndExpand(rr.getProjectId());
    final HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());
    headers.setContentType(MediaType.APPLICATION_JSON);

    if (rr.getStatus() == OperationResponse.RegistrationStatus.CREATED) {
      return new ResponseEntity<>(res, headers, HttpStatus.CREATED);
    }
    throw new RuntimeException(
            "Unexpected status " + rr.getStatus() + ". This should never happen!");
  }

  /**
   * Updates a Redmatch project. The name, rules document and mappings can be updated. However,
   * only the system, code and comment attributes of a mapping can be modified. Mappings are 
   * derived from the rules document so they cannot be modified externally. Any mappings that
   * no longer exist will be ignored.
   * 
   * @param redmatchId The Redmatch project id (the report id).
   * @param project The Redmatch project.
   * @return The response entity.
   */
  @ApiOperation(value = "Updates a Redmatch project.")
  @ApiResponses(value = { 
      @ApiResponse(code = 200, message = "The Redmatch project was updated successfully."),
      @ApiResponse(code = 201, message = "The Redmatch project was created successfully."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") 
  })
  @RequestMapping(value = "/project/{redmatchId}", method = RequestMethod.PUT, 
      consumes = "application/json", produces = "application/json")
  public ResponseEntity<RedmatchProject> updateRedmatchProject(
      @PathVariable String redmatchId,
      @RequestBody RedmatchProject project,
      UriComponentsBuilder b) {
    if (!redmatchId.equals(project.getId())) {
      throw new InvalidProjectException("The id in the Redmatch project parameter (" 
          + project.getReportId() + ") does not match the one in the URL (" + redmatchId + ")");
    }
    final OperationResponse rr = api.updateRedmatchProject(project);
    final RedmatchProject res = api.resolveRedmatchProject(rr.getProjectId(), false);

    final UriComponents uriComponents = b.path("/project/{id}").buildAndExpand(rr.getProjectId());
    final HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());
    headers.setContentType(MediaType.APPLICATION_JSON);

    switch (rr.getStatus()) {
      case UPDATED:
        return new ResponseEntity<>(res, headers, HttpStatus.OK);
      case CREATED:
        return new ResponseEntity<>(res, headers, HttpStatus.CREATED);
      default:
        throw new RuntimeException(
            "Unexpected status " + rr.getStatus() + ". This should never happen!");
    }
  }

  /**
   * Returns a Redmatch project.
   * 
   * @param projectId The id of the Redmatch project.
   * @return The response entity.
   */
  @ApiOperation(value = "Returns a Redmatch project.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 404, message = "The project was not found."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{projectId}", method = RequestMethod.GET, 
  produces = "application/json")
  public ResponseEntity<RedmatchProject> getRedmatchProject(@PathVariable String projectId) {
    final RedmatchProject p = api.resolveRedmatchProject(projectId, false);
    return new ResponseEntity<>(p, HttpStatus.OK);
  }

  /**
   * Returns the transformation rules document.
   * 
   * @param projectId The project id.
   * @return The response entity.
   */
  @ApiOperation(value = "Returns the transformation rules document.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{projectId}/rules", 
      method = RequestMethod.GET, produces = "text/plain")
  public ResponseEntity<String> exportMappingRules(@PathVariable String projectId) {
    return new ResponseEntity<>(api.getRulesDocument(projectId), HttpStatus.OK);
  }

  /**
   * Returns the mappings for a project.
   * 
   * @param accept The accept headers sent by the client.
   * @param projectId The project id.
   * @return The response entity.
   */
  @ApiOperation(value = "Returns all mappings for a project.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 406, message = "The system does not support the requested content type."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{projectId}/mapping",
      method = RequestMethod.GET, produces = {"application/json", "application/vnd.ms-excel" })
  public ResponseEntity<?> getMappings(@RequestHeader(value = "Accept") String accept,
      @PathVariable String projectId) {

    int format = 0; // 0 - not acceptable, 1 - json, 2 - Excel
    if (accept == null || accept.isEmpty()) {
      // We assume this equivalent to */*
      format = 1;
    } else {
      final Set<String> acceptHeaders = WebUtils.parseAcceptHeaders(accept);
      if (acceptHeaders.contains("application/json") || acceptHeaders.contains("*/*")) {
        format = 1;
      } else if (acceptHeaders.contains("application/vnd.ms-excel")) {
        format = 2;
      }
    }

    if (format == 0) {
      return getResponse(HttpStatus.NOT_ACCEPTABLE,
          "Can only handle application/json, application/vnd.ms-excel " + "and */*, but got "
              + accept);
    } else if (format == 1) {
      return new ResponseEntity<>(api.getMappings(projectId), HttpStatus.OK);
    } else {
      final HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Disposition", "attachment; filename=\"study_" + projectId + ".xls\"");

      final byte[] res = api.getMappingsExcel(projectId);
      final ByteArrayResource resource = new ByteArrayResource(res);

      return ResponseEntity.ok().headers(headers).contentLength(res.length)
          .contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(resource);
    }
  }

  /**
   * Imports mappings in Excel format.
   * 
   * @param file The Excel file with the mappings.
   * @param projectId The project id.
   * @return The response entity.
   */
  @ApiOperation(value = "Imports mappings in Excel format.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 406, message = "The uploaded file is empty."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{projectId}/$import-mappings",
      method = RequestMethod.POST, produces = "application/json")
  public ResponseEntity<?> importMappings(
      @RequestParam("file") MultipartFile file,
      @PathVariable String projectId,
      UriComponentsBuilder b) {
    if (!file.isEmpty()) {
      try {
        OperationResponse rr = api.importMappingsExcel(projectId, file.getBytes());
        final RedmatchProject res = api.resolveRedmatchProject(rr.getProjectId(), false);
  
        final UriComponents uriComponents = 
            b.path("/project/{id}").buildAndExpand(rr.getProjectId());
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        headers.setContentType(MediaType.APPLICATION_JSON);
  
        switch (rr.getStatus()) {
          case UPDATED:
            return new ResponseEntity<>(res, headers, HttpStatus.OK);
          case CREATED:
          default:
            throw new RuntimeException(
                "Unexpected status " + rr.getStatus() + ". This should never happen!");
        }
      } catch (IOException e) {
        return getResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
            "There was a problem reading the file.");
      }
    } else {
      return getResponse(HttpStatus.BAD_REQUEST, "The mappings file is empty!");
    }
  }
  
  /**
   * Updates the project's mappings.
   * 
   * @param redmatchId The Redmatch project id.
   * @param mappings mappings The mappings.
   * @return The response entity.
   */
  @ApiOperation(value = "Imports mappings in Excel format.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 400, message = "The mappings are empty."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{redmatchId}/$update-mappings",
      method = RequestMethod.POST, produces = "application/json")
  public ResponseEntity<RedmatchProject> updateMappings(
      @PathVariable String redmatchId,
      @RequestBody List<Mapping> mappings,
      UriComponentsBuilder b) {
    if (mappings != null && !mappings.isEmpty()) {
      OperationResponse rr = api.updateMappings(redmatchId, mappings);
      final RedmatchProject res = api.resolveRedmatchProject(rr.getProjectId(), false);

      final UriComponents uriComponents = b.path("/project/{id}").buildAndExpand(rr.getProjectId());
      final HttpHeaders headers = new HttpHeaders();
      headers.setLocation(uriComponents.toUri());
      headers.setContentType(MediaType.APPLICATION_JSON);

      switch (rr.getStatus()) {
        case UPDATED:
          return new ResponseEntity<>(res, headers, HttpStatus.OK);
        case CREATED:
        default:
          throw new RuntimeException(
              "Unexpected status " + rr.getStatus() + ". This should never happen!");
      }
    } else {
      throw new InvalidMappingsException("The mappings are empty!");
    }
  }
  
  /**
   * Updates the transformation rules document. This is a convenience method so the rules document
   * can be uploaded as a string and not as an attribute of a Redmatch project.
   * 
   * @param rulesDocument The transformation rules document.
   * @param projectId The project id.
   * @return The response entity.
   */
  @ApiOperation(value = "Updates the transformation rules document.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{projectId}/$update-rules",
      method = RequestMethod.POST, produces = "application/json")
  public ResponseEntity<RedmatchProject> updateRulesDocument(
      @PathVariable String projectId, 
      @RequestBody (required = false) String rulesDocument,
      UriComponentsBuilder b) {
    
    // Handle case where rules document wants to be erased
    if (rulesDocument == null) {
      rulesDocument = "";
    }
    
    final OperationResponse rr = api.updateRulesDocument(projectId, rulesDocument);
    final RedmatchProject res = api.resolveRedmatchProject(rr.getProjectId(), false);

    final UriComponents uriComponents = b.path("/project/{id}").buildAndExpand(rr.getProjectId());
    final HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());
    headers.setContentType(MediaType.APPLICATION_JSON);

    switch (rr.getStatus()) {
      case UPDATED:
        return new ResponseEntity<>(res, headers, HttpStatus.OK);
      case CREATED:
        return new ResponseEntity<>(res, headers, HttpStatus.CREATED);
      default:
        throw new RuntimeException(
            "Unexpected status " + rr.getStatus() + ". This should never happen!");
    }
  }
  
  /**
   * Transforms the REDCap data using the current rules and mappings and returns the generated
   * resources in a bundle.
   * 
   * TODO: decide how to deal with warnings.
   *  
   * @param projectId The id of the Redmatch project.
   * @return The response entity.
   */
  @ApiOperation(value = "Transforms the REDCap data using the current rules and mappings and "
      + "returns the generated resources in a bundle.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{projectId}/$transform", 
      method = RequestMethod.POST, produces = "application/json")
  public ResponseEntity<Parameters> transformProject(@PathVariable String projectId) {
    try {
      Parameters res = api.transformProject(projectId);
      return new ResponseEntity<>(res, HttpStatus.OK);
    } catch (Exception e) {
      throw new RuntimeException(e); 
    }
  }
  
  @ApiOperation(value = "Export generated ND-JSON files as a ZIP file.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 406, message = "The system does not support the requested content type."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{projectId}/$export",
      method = RequestMethod.POST, produces = "application/zip" )
  public ResponseEntity<?> exportProject(@RequestHeader(value = "Accept") String accept,
      @PathVariable String projectId) {
    
    if (accept != null && !accept.isEmpty()) {
      final Set<String> acceptHeaders = WebUtils.parseAcceptHeaders(accept);
      if (!acceptHeaders.contains("application/zip") && !acceptHeaders.contains("*/*")) {
        return getResponse(HttpStatus.NOT_ACCEPTABLE, 
            "Can only handle application/zip and */*, but got " + accept + " (parsed: " 
                + acceptHeaders + ")");
      }
    }
    
    final HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=\"study_" + projectId 
        + "_export.zip\"");

    byte[] res;
    try {
      res = api.downloadExportedFiles(projectId);
    } catch (IOException e) {
      // If there is an IO issue we still should send back a 500
      throw new RuntimeException(e);
    }
    final ByteArrayResource resource = new ByteArrayResource(res);

    return ResponseEntity.ok().headers(headers).contentLength(res.length)
        .contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(resource);
  }
  
  
  @ApiOperation(value = "Deletes a Redmatch project.")
  @ApiResponses(value = { @ApiResponse(code = 204, message = "The Redmatch project was deleted "
      + "successfully."),
      @ApiResponse(code = 404, message = "The Redmatch project was not found."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{projectId}", method = RequestMethod.DELETE, 
      produces = "text/plain")
  public ResponseEntity<Void> deleteRedmatchProject(@PathVariable String projectId) {
    api.deleteRedmatchProject(projectId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
  
  /**
   * Updates the REDCap metadata in a Redmatch project.
   *  
   * @param projectId The id of the Redmatch project.
   * @return The response entity.
   */
  @ApiOperation(value = "Updates the REDCap metadata in a Redmatch project.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{projectId}/$update",
      method = RequestMethod.POST, produces = "application/json")
  public ResponseEntity<RedmatchProject> refreshRedcapMetadata(
      @PathVariable String projectId,
      UriComponentsBuilder b) {
    OperationResponse rr = api.refreshRedcapMetadata(projectId);
    final RedmatchProject res = api.resolveRedmatchProject(rr.getProjectId(), false);
    
    final UriComponents uriComponents = b.path("/project/{id}").buildAndExpand(rr.getProjectId());
    final HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());
    headers.setContentType(MediaType.APPLICATION_JSON);

    switch (rr.getStatus()) {
      case UPDATED:
        return new ResponseEntity<>(res, headers, HttpStatus.OK);
      case CREATED:
      default:
        throw new RuntimeException(
            "Unexpected status " + rr.getStatus() + ". This should never happen!");
    }
  }

  /**
   * Returns a configuration file that can be used by the UI.
   *
   * @return The configuration file.
   */
  @ApiOperation(value = "Returns a configuration file that can be used in the UI.")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "The operation completed successfully."),
    @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value="/config/env.js", method = RequestMethod.GET, produces ="application/javascript")
  public ResponseEntity<RawJson> getConfig() {
    String nodeEnv = "development";

    // If profile is docker then set environment to production
    if(Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("docker")))) {
      nodeEnv = "production";
    }

    StringBuilder sb = new StringBuilder();
    sb.append("window._env = {\"NODE_ENV\":\"");
    sb.append(nodeEnv);
    sb.append("\",\"REACT_APP_TERMINOLOGY_URL\":\"");
    sb.append(ontoserverUrl);
    sb.append("\",\"REACT_APP_KEYCLOAK_URL\":\"");
    sb.append(keycloakUrl);
    sb.append("\",\"REACT_APP_KEYCLOAK_REALM\":\"");
    sb.append(keycloakRealm);
    sb.append("\",\"REACT_APP_KEYCLOAK_CLIENT_ID\":\"");
    sb.append(keycloakClientId);
    sb.append("\"};");

    return new ResponseEntity<>(RawJson.from(sb.toString()), HttpStatus.OK);
  }

  @RequestMapping(value = "/username", method = RequestMethod.GET)
  @ResponseBody
  public String currentUserName(Principal principal) {
      return principal.getName();
  }
  
  @ExceptionHandler(HttpException.class)
  private ResponseEntity<OperationOutcome> exceptionHandler(HttpException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(ex.getStatus(), "There was an HTTP error", ex);
  }
  
  @ExceptionHandler(CompilerException.class)
  private ResponseEntity<OperationOutcome> exceptionHandler(CompilerException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.BAD_REQUEST, "There was an issue with the compiler.", ex);
  }
  
  @ExceptionHandler(InvalidMappingsException.class)
  private ResponseEntity<OperationOutcome> exceptionHandler(InvalidMappingsException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.BAD_REQUEST, "There was an issue with the mappings.", ex);
  }

  @ExceptionHandler(ProjectNotFoundException.class)
  private ResponseEntity<OperationOutcome> exceptionHandler(ProjectNotFoundException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.NOT_FOUND, "The project was not found.", ex);
  }

  @ExceptionHandler(RuntimeException.class)
  private ResponseEntity<OperationOutcome> exceptionHandler(RuntimeException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
        "There was an unexpected runtime exception.", ex);
  }
  
  @ExceptionHandler(ProjectAlreadyExistsException.class)
  private ResponseEntity<OperationOutcome> exceptionHandler(ProjectAlreadyExistsException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.UNPROCESSABLE_ENTITY, "The project already exists.", ex);
  }
  
  @ExceptionHandler(InvalidProjectException.class)
  private ResponseEntity<OperationOutcome> exceptionHandler(InvalidProjectException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.BAD_REQUEST, "The project is invalid.", ex);
  }
  
  @ExceptionHandler(RuleApplicationException.class)
  private ResponseEntity<OperationOutcome> exceptionHandler(RuleApplicationException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.BAD_REQUEST, "The project could not be transformed to FHIR. "
        + "Please check your transformation rules.", ex);
  }
  
  @ExceptionHandler(NoMappingFoundException.class)
  private ResponseEntity<OperationOutcome> exceptionHandler(NoMappingFoundException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.BAD_REQUEST, "The project could not be transformed to FHIR "
        + "because a mapping is missing. Please check your mappings.", ex); 
  }
  
  private ResponseEntity<OperationOutcome> getResponse(HttpStatus status, String msg, Exception e) {
    OperationOutcome oo = new OperationOutcome();
    oo
      .addIssue()
      .setSeverity(IssueSeverity.ERROR)
      .setCode(IssueType.EXCEPTION)
      .setDiagnostics(msg + " [" + e.getLocalizedMessage() + "]");
    return new ResponseEntity<>(oo, getHeaders(), status);
  }

  private ResponseEntity<String> getResponse(HttpStatus status, String message) {
    return new ResponseEntity<>(gson.toJson(message), getHeaders(), status);
  }

  private HttpHeaders getHeaders() {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    return httpHeaders;
  }
  
  private List<String> parseElements(String elements) {
    final List<String> elems = new ArrayList<>();
    if (elements != null) {
      Collections.addAll(elems, elements.split("[,]"));
    }
    return elems;
  }

}
