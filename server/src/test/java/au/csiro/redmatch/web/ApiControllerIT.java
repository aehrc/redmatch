/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.web;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import au.csiro.redmatch.Application;
import au.csiro.redmatch.ResourceLoader;
import au.csiro.redmatch.importer.RedcapImporter;
import au.csiro.redmatch.model.Mapping;
import au.csiro.redmatch.model.RedmatchProject;
import au.csiro.redmatch.validation.MockTerminolgyClient;
import au.csiro.redmatch.validation.RedmatchGrammarValidator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the REST controller.
 * 
 * @author Alejandro Metke
 */
@TestExecutionListeners({
  DependencyInjectionTestExecutionListener.class,
  ApiControllerIT.class
})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment=WebEnvironment.RANDOM_PORT)
public class ApiControllerIT extends AbstractTestExecutionListener {
  
  /** Logger */
  private static final Log log = LogFactory.getLog(ApiControllerIT.class);
  
  /**
   * The port where the embedded server will run on.
   */
  @Value("${local.server.port}")
  public int port;
  
  @Autowired
  ApplicationContext ctx;
  
  /**
   * A template used by the Spring framework to do REST calls.
   */
  private final RestTemplate template = new RestTemplateBuilder()
    .errorHandler(new DefaultResponseErrorHandler() {
        @Override
        protected boolean hasError(HttpStatus statusCode) {
            if (!statusCode.is2xxSuccessful()) {
                log.error("Request failed: (" + statusCode.value() + ") " 
                    + statusCode.getReasonPhrase());
            }
            return false;
        }
    })
    .build();

  /**
   * Instance-level method to set up everything needed for the integration tests. Runs only once 
   * before any of the test methods.
   *
   * @param testContext
   * @throws IOException
   */
  @Override
  public void beforeTestClass(TestContext testContext) throws IOException {
    // Get the Spring beans we need
    final ApplicationContext appCtx = testContext.getApplicationContext();
    // Set the mock terminology client
    log.info("Setting terminology client to mock implementation.");
    RedmatchGrammarValidator rgv = appCtx.getBean(RedmatchGrammarValidator.class);
    rgv.setClient(new MockTerminolgyClient());
  }

  @Test
  public void testCreateProject() throws URISyntaxException {
    log.info("Running testCreateProject");
    // Set the mock REDCap client
    RedcapImporter ri = ctx.getBean(RedcapImporter.class);
    ri.setRedcapClient(new MockRedcapClient("bug"));
    // Create project
    RedmatchProject body = new RedmatchProject(UUID.randomUUID().toString(), "http://dummyredcapurl.com/api");
    body.setName("Redmatch bug");
    body.setToken("xxx");
    RequestEntity<RedmatchProject> request = RequestEntity
            .post(new URI("http://localhost:" + port + "/project"))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    ResponseEntity<RedmatchProject> response = template.exchange(request, RedmatchProject.class);
    RedmatchProject resp = response.getBody();
    assertEquals(26, resp.getFields().size());
  }
  
  @Test
  public void testUpdateMappings() throws URISyntaxException {
    log.info("Running testUpdateMappings");
    // Set the mock REDCap client
    RedcapImporter ri = ctx.getBean(RedcapImporter.class);
    ri.setRedcapClient(new MockRedcapClient("tutorial"));
    
    // Create project
    RedmatchProject body = new RedmatchProject(UUID.randomUUID().toString(), "http://dummyredcapurl.com/api");
    body.setName("Tutorial IT");
    body.setToken("xxx");
    RequestEntity<RedmatchProject> request = RequestEntity
        .post(new URI("http://localhost:" + port + "/project"))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
    ResponseEntity<RedmatchProject> response = template.exchange(request, RedmatchProject.class);
    RedmatchProject resp = response.getBody();
    int numFields = resp.getFields().size();
    assertTrue(numFields > 0);

    final String projectId = resp.getId();
    
    // Update rules
    RequestEntity<String> rulesRequest = RequestEntity
        .post(new URI("http://localhost:" + port + "/project/" + projectId + "/$update-rules"))
        .accept(MediaType.APPLICATION_JSON)
        .body(new ResourceLoader().loadRulesString("tutorial"));
    response = template.exchange(rulesRequest, RedmatchProject.class);
    
    resp = response.getBody();
    assertEquals(numFields, resp.getFields().size());
    assertEquals(18, resp.getMappings().size());
    
    // Populate mappings and update project
    List<Mapping> mappings = resp.getMappings();
    populateMappings(mappings);
    RequestEntity<List<Mapping>> mappingsRequest = RequestEntity
        .post(new URI("http://localhost:" + port + "/project/" + projectId + "/$update-mappings"))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(mappings);
    response = template.exchange(mappingsRequest, RedmatchProject.class);
    resp = response.getBody();
    assertEquals(18, resp.getMappings().size());
    
    // Replace rules with short version - extra mappings should be inactivated
    rulesRequest = RequestEntity
        .post(new URI("http://localhost:" + port + "/project/" + projectId + "/$update-rules"))
        .accept(MediaType.APPLICATION_JSON)
        .body(new ResourceLoader().loadRulesString("tutorial_short"));
    response = template.exchange(rulesRequest, RedmatchProject.class);
    resp = response.getBody();
    assertEquals(6, resp.getMappings().size());
  }

  @Test
  public void testUpdateMappingsBug() throws URISyntaxException {
    log.info("Running testUpdateMappingsBug");
    // Set the mock REDCap client
    RedcapImporter ri = ctx.getBean(RedcapImporter.class);
    ri.setRedcapClient(new MockRedcapClient("bug"));
    // Create project
    RedmatchProject body = new RedmatchProject(UUID.randomUUID().toString(), "http://dummyredcapurl.com/api");
    body.setName("Redmatch bug");
    body.setToken("xxx");
    RequestEntity<RedmatchProject> request = RequestEntity
        .post(new URI("http://localhost:" + port + "/project"))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body);
    ResponseEntity<RedmatchProject> response = template.exchange(request, RedmatchProject.class);
    RedmatchProject resp = response.getBody();
    final String projectId = resp.getId();
    
    // Update rules
    RequestEntity<String> rulesRequest = RequestEntity
        .post(new URI("http://localhost:" + port + "/project/" + projectId + "/$update-rules"))
        .accept(MediaType.APPLICATION_JSON)
        .body(new ResourceLoader().loadRulesString("bug"));
    response = template.exchange(rulesRequest, RedmatchProject.class);
    
    resp = response.getBody();
    assertEquals(4, resp.getMappings().size());
    
    // Populate mappings and update project
    List<Mapping> mappings = resp.getMappings();
    populateMappingsBugFirstPass(mappings);
    RequestEntity<List<Mapping>> mappingsRequest = RequestEntity
        .post(new URI("http://localhost:" + port + "/project/" + projectId + "/$update-mappings"))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(mappings);
    response = template.exchange(mappingsRequest, RedmatchProject.class);
    resp = response.getBody();
    assertEquals(4, resp.getMappings().size());
    
    Mapping male = resp.getMappings().get(0);
    Mapping female = resp.getMappings().get(1);
    Mapping indeterminate = resp.getMappings().get(2);
    Mapping ophthal = resp.getMappings().get(3);
    
    assertEquals("http://snomed.info/sct", male.getTargetSystem());
    assertEquals("248153007", male.getTargetCode());
    
    assertEquals("http://snomed.info/sct", female.getTargetSystem());
    assertEquals("248152002", female.getTargetCode());
    
    assertEquals("http://snomed.info/sct", indeterminate.getTargetSystem());
    assertEquals("32570681000036100", indeterminate.getTargetCode());
    
    assertNull(ophthal.getTargetSystem());
    assertNull(ophthal.getTargetCode());
    
    mappings = resp.getMappings();
    populateMappingsBugSecondPass(mappings);
    mappingsRequest = RequestEntity
        .post(new URI("http://localhost:" + port + "/project/" + projectId + "/$update-mappings"))
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .body(mappings);
    response = template.exchange(mappingsRequest, RedmatchProject.class);
    resp = response.getBody();
    assertEquals(4, resp.getMappings().size());
    
    male = resp.getMappings().get(0);
    female = resp.getMappings().get(1);
    indeterminate = resp.getMappings().get(2);
    ophthal = resp.getMappings().get(3);
    
    assertEquals("http://purl.obolibrary.org/obo/hp.owl", ophthal.getTargetSystem());
    assertEquals("HP:0000602", ophthal.getTargetCode());
    
    assertEquals("http://snomed.info/sct", male.getTargetSystem());
    assertEquals("248153007", male.getTargetCode());
    
    assertEquals("http://snomed.info/sct", female.getTargetSystem());
    assertEquals("248152002", female.getTargetCode());
    
    assertEquals("http://snomed.info/sct", indeterminate.getTargetSystem());
    assertEquals("32570681000036100", indeterminate.getTargetCode());
  }
  
  private void populateMappingsBugFirstPass(List<Mapping> mappings) {
    for (Mapping mapping : mappings) {
      String fieldId = mapping.getRedcapFieldId();
      if (fieldId.equals("dem_sex___1")) {
        mapping.setTargetSystem("http://snomed.info/sct");
        mapping.setTargetCode("248153007");
      } else if (fieldId.equals("dem_sex___2")) {
        mapping.setTargetSystem("http://snomed.info/sct");
        mapping.setTargetCode("248152002");
      } else if (fieldId.equals("dem_sex___3")) {
        mapping.setTargetSystem("http://snomed.info/sct");
        mapping.setTargetCode("32570681000036100");
      }
    }
  }
  
  private void populateMappingsBugSecondPass(List<Mapping> mappings) {
    for (Mapping mapping : mappings) {
      String fieldId = mapping.getRedcapFieldId();
      if (fieldId.equals("dem_sex___1")) {
        mapping.setTargetSystem("http://snomed.info/sct");
        mapping.setTargetCode("248153007");
      } else if (fieldId.equals("dem_sex___2")) {
        mapping.setTargetSystem("http://snomed.info/sct");
        mapping.setTargetCode("248152002");
      } else if (fieldId.equals("dem_sex___3")) {
        mapping.setTargetSystem("http://snomed.info/sct");
        mapping.setTargetCode("32570681000036100");
      } else if (fieldId.equals("mito_mw_ophthal")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0000602");
      }
    }
  }
  
  private void populateMappings (List<Mapping> mappings) {
    for (Mapping mapping : mappings) {
      String fieldId = mapping.getRedcapFieldId();
      if (fieldId.equals("pat_sex___1")) {
        mapping.setTargetCode("male");
      } else if (fieldId.equals("pat_sex___2")) {
        mapping.setTargetCode("female");
      } else if (fieldId.equals("phenotype___1")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001558");
        mapping.setTargetDisplay("Decreased fetal movement");
      } else if (fieldId.equals("phenotype___2")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001270");
        mapping.setTargetDisplay("Motor delay");
      } else if (fieldId.equals("phenotype___3")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0031910");
        mapping.setTargetDisplay("Abnormal cranial nerve physiology");
      } else if (fieldId.equals("phenotype___4")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0012587");
        mapping.setTargetDisplay("Macroscopic hematuria");
      } else if (fieldId.equals("m_weak")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001324");
        mapping.setTargetDisplay("Muscle weakness");
      } else if (fieldId.equals("facial")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0010628");
        mapping.setTargetDisplay("Facial palsy");
      } else if (fieldId.equals("ptosis")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0000508");
        mapping.setTargetDisplay("Ptosis");
      } else if (fieldId.equals("oph")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0000602");
        mapping.setTargetDisplay("Ophthalmoplegia");
      } else if (fieldId.equals("left_bicep___1")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001252");
        mapping.setTargetDisplay("Muscular hypotonia");
      } else if (fieldId.equals("left_bicep___3")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001276");
        mapping.setTargetDisplay("Hypertonia");
      } else if (fieldId.equals("right_bicep___1")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001252");
        mapping.setTargetDisplay("Muscular hypotonia");
      } else if (fieldId.equals("right_bicep___3")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001276");
        mapping.setTargetDisplay("Hypertonia");
      } else if (fieldId.equals("left_tricep___1")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001252");
        mapping.setTargetDisplay("Muscular hypotonia");
      } else if (fieldId.equals("left_tricep___3")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001276");
        mapping.setTargetDisplay("Hypertonia");
      } else if (fieldId.equals("right_tricep___1")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001252");
        mapping.setTargetDisplay("Muscular hypotonia");
      } else if (fieldId.equals("right_tricep___3")) {
        mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
        mapping.setTargetCode("HP:0001276");
        mapping.setTargetDisplay("Hypertonia");
      }
    }
  }
}
