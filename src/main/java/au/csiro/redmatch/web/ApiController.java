package au.csiro.redmatch.web;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
    return new ResponseEntity<List<RedmatchProject>>(projects, HttpStatus.OK);
  }

  /**
   * Registers a Redmatch project.
   * 
   * @param redcapId The REDCap project id.
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
  @Transactional
  public ResponseEntity<RedmatchProject> createRedmatchProject(
      @RequestBody RedmatchProject project,
      UriComponentsBuilder b) {
    
    final OperationResponse rr = api.createRedmatchProject(project);
    final RedmatchProject res = api.resolveRedmatchProject(rr.getProjectId());

    final UriComponents uriComponents = b.path("/project/{id}").buildAndExpand(rr.getProjectId());
    final HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());
    headers.setContentType(MediaType.APPLICATION_JSON);

    switch (rr.getStatus()) {
      case CREATED:
        return new ResponseEntity<RedmatchProject>(res, headers, HttpStatus.CREATED);
      default:
        throw new RuntimeException(
            "Unexpected status " + rr.getStatus() + ". This should never happen!");
    }
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
  @Transactional
  public ResponseEntity<RedmatchProject> updateRedmatchProject(
      @PathVariable String redmatchId,
      @RequestBody RedmatchProject project,
      UriComponentsBuilder b) {
    if (!redmatchId.equals(project.getId())) {
      throw new InvalidProjectException("The id in the Redmatch project parameter (" 
          + project.getReportId() + ") does not match the one in the URL (" + redmatchId + ")");
    }
    final OperationResponse rr = api.updateRedmatchProject(project);
    final RedmatchProject res = api.resolveRedmatchProject(rr.getProjectId());

    final UriComponents uriComponents = b.path("/project/{id}").buildAndExpand(rr.getProjectId());
    final HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());
    headers.setContentType(MediaType.APPLICATION_JSON);

    switch (rr.getStatus()) {
      case UPDATED:
        return new ResponseEntity<RedmatchProject>(res, headers, HttpStatus.OK);
      case CREATED:
        return new ResponseEntity<RedmatchProject>(res, headers, HttpStatus.CREATED);
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
    final RedmatchProject p = api.resolveRedmatchProject(projectId);
    return new ResponseEntity<RedmatchProject>(p, HttpStatus.OK);
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
    return new ResponseEntity<String>(api.getRulesDocument(projectId), HttpStatus.OK);
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
  @RequestMapping(value = "project/{projectId}/mapping", 
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
      return new ResponseEntity<List<Mapping>>(api.getMappings(projectId), HttpStatus.OK);
    } else if (format == 2) {
      final HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Disposition", "attachment; filename=\"study_" + projectId + ".xls\"");

      final byte[] res = api.getMappingsExcel(projectId);
      final ByteArrayResource resource = new ByteArrayResource(res);

      return ResponseEntity.ok().headers(headers).contentLength(res.length)
          .contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(resource);
    } else {
      throw new RuntimeException(
          "Unexpected format value " + format + ". This should never happen.");
    }
  }

  /**
   * Imports mappings in Excel format.
   * 
   * @param file The Excel file with the mappings.
   * @param projectId The project id.
   * @return The response entity.
   * @throws IOException If an IO error happens.
   */
  @ApiOperation(value = "Imports mappings in Excel format.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 406, message = "The uploaded file is empty."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "project/{projectId}/$import-mappings", 
      method = RequestMethod.POST, produces = "text/plain")
  @Transactional
  public ResponseEntity<String> importMappings(@RequestParam("file") MultipartFile file,
      @PathVariable String projectId) {
    if (!file.isEmpty()) {
      try {
        api.importMappingsExcel(projectId, file.getBytes());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return getResponse(HttpStatus.OK, "Mappings were imported successfully");
    } else {
      return getResponse(HttpStatus.BAD_REQUEST, "The uploaded file is empty.");
    }
  }
  
  /**
   * Updates the transformation rules document. This is a convenience method so the rules document
   * can be uploaded as a string and not as an attribute of a Redmatch project.
   * 
   * @param rulesDocument The transformation rules document.
   * @param projectId The project id.
   * @return The response entity.
   * @throws IOException If an IO error happens.
   */
  @ApiOperation(value = "Updates the transformation rules document.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The operation completed successfully."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "project/{projectId}/$update-rules", 
      method = RequestMethod.POST, produces = "application/json")
  @Transactional
  public ResponseEntity<RedmatchProject> updateRulesDocument(
      @PathVariable String projectId, 
      @RequestBody String rulesDocument,
      UriComponentsBuilder b) {
    
    final OperationResponse rr = api.updateRulesDocument(projectId, rulesDocument);
    final RedmatchProject res = api.resolveRedmatchProject(rr.getProjectId());

    final UriComponents uriComponents = b.path("/project/{id}").buildAndExpand(rr.getProjectId());
    final HttpHeaders headers = new HttpHeaders();
    headers.setLocation(uriComponents.toUri());
    headers.setContentType(MediaType.APPLICATION_JSON);

    switch (rr.getStatus()) {
      case UPDATED:
        return new ResponseEntity<RedmatchProject>(res, headers, HttpStatus.OK);
      case CREATED:
        return new ResponseEntity<RedmatchProject>(res, headers, HttpStatus.CREATED);
      default:
        throw new RuntimeException(
            "Unexpected status " + rr.getStatus() + ". This should never happen!");
    }
  }
  
  /**
   * Transforms the REDCap data using the current rules and mappings and returns the generated
   * resources in a bundle.
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
  public ResponseEntity<Bundle>  transformProject(@PathVariable String projectId) {
    final Bundle res = api.transformProject(projectId);
    return new ResponseEntity<Bundle>(res, HttpStatus.OK);
  }
  
  @ApiOperation(value = "Deletes a Redmatch project.")
  @ApiResponses(value = { @ApiResponse(code = 204, message = "The Redmatch project was deleted "
      + "successfully."),
      @ApiResponse(code = 404, message = "The Redmatch project was not found."),
      @ApiResponse(code = 500, message = "An unexpected server error occurred.") })
  @RequestMapping(value = "/project/{projectId}", method = RequestMethod.DELETE, 
      produces = "text/plain")
  @Transactional
  public ResponseEntity<Void> deleteRedmatchProject(@PathVariable String projectId) {
    api.deleteRedmatchProject(projectId);
    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
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
  @RequestMapping(value = "project/{projectId}/$update", 
      method = RequestMethod.POST, produces = "application/json")
  public ResponseEntity<Void> refreshRedcapData(@PathVariable String projectId) {
    api.refreshRedcapMetadata(projectId);
    return new ResponseEntity<Void>(HttpStatus.OK);
  }
  
  @RequestMapping(value = "/username", method = RequestMethod.GET)
  @ResponseBody
  public String currentUserName(Principal principal) {
      return principal.getName();
  }
  
  @ExceptionHandler(HttpException.class)
  private ResponseEntity<String> exceptionHandler(HttpException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(ex.getStatus(), ex.getMessage());
  }
  
  @ExceptionHandler(CompilerException.class)
  private ResponseEntity<String> exceptionHandler(CompilerException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ProjectNotFoundException.class)
  private ResponseEntity<String> exceptionHandler(ProjectNotFoundException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(RuntimeException.class)
  private ResponseEntity<String> exceptionHandler(RuntimeException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }
  
  @ExceptionHandler(InvalidProjectException.class)
  private ResponseEntity<String> exceptionHandler(InvalidProjectException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }
  
  @ExceptionHandler(RuleApplicationException.class)
  private ResponseEntity<String> exceptionHandler(RuleApplicationException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.BAD_REQUEST, "The project could not be transformed to FHIR. "
        + "Please check your transformation rules. Error message: " + ex.getLocalizedMessage());
  }
  
  @ExceptionHandler(NoMappingFoundException.class)
  private ResponseEntity<String> exceptionHandler(NoMappingFoundException ex) {
    log.error(ex.getMessage(), ex);
    return getResponse(HttpStatus.BAD_REQUEST, "The project could not be transformed to FHIR "
        + "because a mapping is missing. Please check your mappings. Error message: " 
        + ex.getLocalizedMessage());
  }

  private ResponseEntity<String> getResponse(HttpStatus status, String message) {
    return new ResponseEntity<String>(gson.toJson(message), getHeaders(), status);
  }

  private HttpHeaders getHeaders() {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    return httpHeaders;
  }
  
  private List<String> parseElements(String elements) {
    final List<String> elems = new ArrayList<>();
    if (elements != null) {
      for (String e : elements.split("[,]")) {
        elems.add(e);
      }
    }
    return elems;
  }

}
