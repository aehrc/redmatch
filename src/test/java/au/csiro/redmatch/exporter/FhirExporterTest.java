/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.exporter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.csiro.redmatch.AbstractRedmatchTest;
import au.csiro.redmatch.client.ITerminologyServer;
import au.csiro.redmatch.compiler.RedmatchCompiler;
import au.csiro.redmatch.exporter.FhirExporter;
import au.csiro.redmatch.importer.RedcapImporter;
import au.csiro.redmatch.model.Annotation;
import au.csiro.redmatch.model.Mapping;
import au.csiro.redmatch.model.Metadata;
import au.csiro.redmatch.model.Row;
import au.csiro.redmatch.model.grammar.redmatch.Document;
import au.csiro.redmatch.validation.MockTerminolgyServer;

/**
 * FHIR exporter unit tests.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource("classpath:application.properties")
@SpringBootTest
public class FhirExporterTest extends AbstractRedmatchTest {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(FhirExporterTest.class);
  
  @Autowired
  private FhirExporter exporter;
  
  @Autowired
  private RedmatchCompiler compiler;
  
  @Autowired
  private RedcapImporter redcapImporter;
  
  private ITerminologyServer mockTerminologyServer = new MockTerminolgyServer();
  
  @Before
  public void hookMock() {
    log.info("Setting mock terminology server");
    compiler.getValidator().setClient(mockTerminologyServer);
  }
  
  @Test
  public void testCreateClinicalResourcesFromRules() {
    Metadata metadata = this.loadMetadata();
    List<Row> rows = this.loadData();
    String rules = this.loadRules();
    Document rulesDocument = compiler.compile(rules, metadata);
    assertNotNull(rulesDocument);
    final List<Annotation> compilationErrors = compiler.getErrorMessages();
    for (Annotation ann : compilationErrors) {
      System.out.println(ann);
    }
    assertTrue(compilationErrors.isEmpty());
    
    List<Mapping> mappings = redcapImporter.generateMappings(metadata, 
        rulesDocument.getReferencedFields(metadata));
    
    populateMappings(mappings);
    
    Map<String, DomainResource> res = exporter.createClinicalResourcesFromRules(metadata, rows, 
        mappings, rulesDocument);
    
    System.out.println(res.keySet());
    
    // Patient 1
    assertTrue(res.containsKey("p-1"));
    DomainResource dr = res.get("p-1");
    assertTrue(dr instanceof Patient);
    Patient p1 = (Patient) dr;
    assertTrue(p1.hasIdentifier());
    Identifier ident = p1.getIdentifierFirstRep();
    assertTrue(ident.hasType());
    assertTrue(ident.getType().hasCoding());
    assertEquals("http://hl7.org/fhir/v2/0203", ident.getType().getCodingFirstRep().getSystem());
    assertEquals("MC", ident.getType().getCodingFirstRep().getCode());
    assertEquals("Medicare Number", ident.getType().getText());
    assertEquals("http://ns.electronichealth.net.au/id/medicare-number", ident.getSystem());
    assertEquals("12345678911", ident.getValue());
    assertTrue(p1.hasDeceasedBooleanType());
    assertEquals(false, p1.getDeceasedBooleanType().booleanValue());
    assertEquals(p1.getGender(), AdministrativeGender.MALE);
    
    // c1-1 and c2-1
    assertTrue(res.containsKey("c1-1"));
    dr = res.get("c1-1");
    assertTrue(dr instanceof Condition);
    Condition c11 = (Condition) dr;
    assertTrue(c11.hasCode());
    Coding cod = c11.getCode().getCodingFirstRep();
    assertEquals("http://snomed.info/sct", cod.getSystem());
    assertEquals("74400008", cod.getCode());
    assertEquals("Appendicitis", cod.getDisplay());
    
    assertTrue(res.containsKey("c2-1"));
    dr = res.get("c2-1");
    assertTrue(dr instanceof Condition);
    Condition c21 = (Condition) dr;
    assertTrue(c21.hasCode());
    assertEquals("CAKUT", c21.getCode().getText());
    
    // obs1-1 and obs3-1    
    testObservation(res, "obs1-1", "http://purl.obolibrary.org/obo/hp.owl", "HP:0001558", 
        "Decreased fetal movement", "POS");
    
    testObservation(res, "obs3-1", "http://purl.obolibrary.org/obo/hp.owl", "HP:0031910", 
        "Abnormal cranial nerve physiology", "POS");
    
    testObservation(res, "m_weak-1", "http://purl.obolibrary.org/obo/hp.owl", "HP:0001324", 
        "Muscle weakness", "POS");
    
    testObservation(res, "facial-1", "http://purl.obolibrary.org/obo/hp.owl", "HP:0010628", 
        "Facial palsy", "NEG");
    
    testObservation(res, "ptosis-1", "http://purl.obolibrary.org/obo/hp.owl", "HP:0000508", 
        "Ptosis", "POS");
    
    testObservation(res, "oph-1", "http://purl.obolibrary.org/obo/hp.owl", "HP:0000602", 
        "Ophthalmoplegia", "NEG");
    
    testObservation(res, "right_tricep-1", "http://purl.obolibrary.org/obo/hp.owl", "HP:0001252", 
        "Muscular hypotonia", "POS", "699996001", "Triceps brachii muscle and/or tendon structure", 
        false);
    
    // Patient 2
    assertTrue(res.containsKey("p-2"));
    dr = res.get("p-2");
    assertTrue(dr instanceof Patient);
    Patient p2 = (Patient) dr;
    assertTrue(p2.hasIdentifier());
    ident = p2.getIdentifierFirstRep();
    assertTrue(ident.hasType());
    assertTrue(ident.getType().hasCoding());
    assertEquals("http://hl7.org/fhir/v2/0203", ident.getType().getCodingFirstRep().getSystem());
    assertEquals("MC", ident.getType().getCodingFirstRep().getCode());
    assertEquals("Medicare Number", ident.getType().getText());
    assertEquals("http://ns.electronichealth.net.au/id/medicare-number", ident.getSystem());
    assertEquals("9876543211", ident.getValue());
    assertTrue(p2.hasDeceasedDateTimeType());
    assertEquals(2, p2.getDeceasedDateTimeType().getDay().intValue());
    assertEquals(3, p2.getDeceasedDateTimeType().getMonth().intValue());
    assertEquals(2020, p2.getDeceasedDateTimeType().getYear().intValue());
    assertEquals(p2.getGender(), AdministrativeGender.FEMALE);
    
    // c1-2
    assertTrue(res.containsKey("c1-2"));
    dr = res.get("c1-2");
    assertTrue(dr instanceof Condition);
    Condition c12 = (Condition) dr;
    assertTrue(c12.hasCode());
    cod = c12.getCode().getCodingFirstRep();
    assertEquals("http://snomed.info/sct", cod.getSystem());
    assertEquals("73211009", cod.getCode());
    assertEquals("Diabetes mellitus", cod.getDisplay());
    
    testObservation(res, "m_weak-2", "http://purl.obolibrary.org/obo/hp.owl", "HP:0001324", 
        "Muscle weakness", "NEG");
    
    testObservation(res, "left_bicep-2", "http://purl.obolibrary.org/obo/hp.owl", "HP:0001276", 
        "Hypertonia", "POS", "699956003", "Biceps brachii muscle and/or tendon structure", true);
  }

  private void testObservation(Map<String, DomainResource> res, String id, String system, String code, String display, String interpretation) {
    DomainResource dr  = res.get(id);
    assertTrue(res.containsKey(id));
    assertTrue(dr instanceof Observation);
    Observation obs = (Observation) dr;
    assertTrue(obs.hasStatus());
    assertEquals(ObservationStatus.FINAL, obs.getStatus());
    assertTrue(obs.hasCode());
    Coding cod = obs.getCode().getCodingFirstRep();
    assertEquals(system, cod.getSystem());
    assertEquals(code, cod.getCode());
    assertEquals(display, cod.getDisplay());
    assertTrue(obs.hasInterpretation());
    CodeableConcept inter = obs.getInterpretationFirstRep();
    assertTrue(inter.hasCoding());
    cod = inter.getCodingFirstRep();
    assertEquals("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation", 
        cod.getSystem());
    assertEquals(interpretation, cod.getCode());
  }
  
  private void testObservation(Map<String, DomainResource> res, String id, String system, 
      String code, String display, String interpretation, String bodySiteCode, 
      String bodySiteDisplay, boolean lateralityLeft) {
    DomainResource dr  = res.get(id);
    assertTrue(res.containsKey(id));
    assertTrue(dr instanceof Observation);
    Observation obs = (Observation) dr;
    assertTrue(obs.hasStatus());
    assertEquals(ObservationStatus.FINAL, obs.getStatus());
    assertTrue(obs.hasCode());
    Coding cod = obs.getCode().getCodingFirstRep();
    assertEquals(system, cod.getSystem());
    assertEquals(code, cod.getCode());
    assertEquals(display, cod.getDisplay());
    assertTrue(obs.hasInterpretation());
    CodeableConcept inter = obs.getInterpretationFirstRep();
    assertTrue(inter.hasCoding());
    cod = inter.getCodingFirstRep();
    assertEquals("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation", 
        cod.getSystem());
    assertEquals(interpretation, cod.getCode());
    
    assertTrue(obs.hasBodySite());
    CodeableConcept bodySite = obs.getBodySite();
    assertTrue(bodySite.hasCoding());
    cod = bodySite.getCodingFirstRep();
    assertEquals("http://snomed.info/sct", cod.getSystem());
    assertEquals(bodySiteCode, cod.getCode());
    assertEquals(bodySiteDisplay, cod.getDisplay());
    
    assertTrue(obs.hasComponent());
    ObservationComponentComponent occ = obs.getComponentFirstRep();
    assertTrue(occ.hasCode());
    CodeableConcept cCode = occ.getCode();
    assertTrue(cCode.hasCoding());
    cod = cCode.getCodingFirstRep();
    assertEquals("http://purl.obolibrary.org/obo/hp.owl", cod.getSystem());
    assertEquals("HP:0012831", cod.getCode());
    assertEquals("Laterality", cod.getDisplay());
    
    assertTrue(occ.hasValueCodeableConcept());
    CodeableConcept val = occ.getValueCodeableConcept();
    assertTrue(val.hasCoding());
    cod = val.getCodingFirstRep();
    assertEquals("http://purl.obolibrary.org/obo/hp.owl", cod.getSystem());
    if (lateralityLeft) {
      assertEquals("HP:0012835", cod.getCode());
      assertEquals("Left", cod.getDisplay());
    } else {
      assertEquals("HP:0012834", cod.getCode());
      assertEquals("Right", cod.getDisplay());
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
