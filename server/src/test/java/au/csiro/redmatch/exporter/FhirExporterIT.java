/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.exporter;

import au.csiro.redmatch.AbstractRedmatchTest;
import au.csiro.redmatch.api.RedmatchApi;
import au.csiro.redmatch.client.ITerminologyClient;
import au.csiro.redmatch.compiler.RedmatchCompiler;
import au.csiro.redmatch.model.Annotation;
import au.csiro.redmatch.model.Mapping;
import au.csiro.redmatch.model.RedmatchProject;
import au.csiro.redmatch.model.Row;
import au.csiro.redmatch.model.grammar.redmatch.Document;
import au.csiro.redmatch.validation.MockTerminolgyClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Observation.ObservationComponentComponent;
import org.hl7.fhir.r4.model.Observation.ObservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FHIR exporter unit tests.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:application.properties")
@SpringBootTest
public class FhirExporterIT extends AbstractRedmatchTest {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(FhirExporterIT.class);
  
  @Autowired
  private FhirExporter exporter;
  
  @Autowired
  private RedmatchCompiler compiler;

  @Autowired
  private RedmatchApi redmatchApi;
  
  private final ITerminologyClient mockTerminologyServer = new MockTerminolgyClient();
  
  @BeforeEach
  public void hookMock() {
    log.info("Setting mock terminology server");
    compiler.getValidator().setClient(mockTerminologyServer);
  }
  
  @Test
  public void testCreateClinicalResourcesFromRulesDataMappings() {
    RedmatchProject project = this.loadProject("data_mappings");
    List<Row> rows = this.loadData("data_mappings");
    
    Document rulesDocument = compiler.compile(project);
    assertNotNull(rulesDocument);
    final List<Annotation> compilationErrors = project.getIssues();
    for (Annotation ann : compilationErrors) {
      System.out.println(ann);
    }
    assertTrue(compilationErrors.isEmpty());
    
    List<Mapping> mappings = redmatchApi.generateMappings(project.getFields(),
        rulesDocument.getReferencedFields(project), rows);
    
    populateDataMappings(mappings);
    project.addMappings(mappings);
    
    Map<String, DomainResource> res = exporter.createClinicalResourcesFromRules(project, rulesDocument, rows);
    
    System.out.println(res.keySet());
    
    assertEquals(4, res.keySet().size());

    assertTrue(res.containsKey("pat-gene-1"));
    DomainResource dr = res.get("pat-gene-1");
    assertTrue(dr instanceof Observation);
    Observation obs1 = (Observation) dr;
    assertTrue(obs1.hasComponent());
    Type vt = obs1.getComponentFirstRep().getValue();
    assertTrue(vt instanceof CodeableConcept);
    CodeableConcept cc = (CodeableConcept) vt;
    assertTrue(cc.hasCoding());
    Coding c = cc.getCodingFirstRep();
    assertEquals("http://www.genenames.org/geneId", c.getSystem());
    assertEquals("HGNC:1100", c.getCode());
    assertEquals("BRCA1", c.getDisplay());
    
    assertTrue(res.containsKey("pat-gene-2"));
    dr = res.get("pat-gene-2");
    assertTrue(dr instanceof Observation);
    Observation obs2 = (Observation) dr;
    assertTrue(obs2.hasComponent());
    vt = obs2.getComponentFirstRep().getValue();
    assertTrue(vt instanceof CodeableConcept);
    cc = (CodeableConcept) vt;
    assertTrue(cc.hasCoding());
    c = cc.getCodingFirstRep();
    assertEquals("http://www.genenames.org/geneId", c.getSystem());
    assertEquals("HGNC:1101", c.getCode());
    assertEquals("BRCA2", c.getDisplay());
    
    assertTrue(res.containsKey("rstud"));
    dr = res.get("rstud");
    assertTrue(dr instanceof ResearchStudy);
    ResearchStudy rs = (ResearchStudy) dr;
    assertTrue(rs.hasIdentifier());
    assertEquals(1, rs.getIdentifier().size());
    Identifier id = rs.getIdentifierFirstRep();
    assertTrue(id.hasType());
    CodeableConcept idType = id.getType();
    assertTrue(idType.hasCoding());
    Coding cod = idType.getCodingFirstRep();
    assertEquals("http://genomics.ontoserver.csiro.au/clipi/CodeSystem/IdentifierType", cod.getSystem());
    assertEquals("RSI", cod.getCode());
    assertEquals("Research study identifier", cod.getDisplay());
    assertEquals("http://www.australiangenomics.org.au/id/research-study", id.getSystem());
    assertEquals("mito", id.getValue());
    
    assertTrue(res.containsKey("enc"));
    dr = res.get("enc");
    assertTrue(dr instanceof Encounter);
    Encounter enc = (Encounter) dr;
    assertTrue(enc.hasClass_());
    Coding cl = enc.getClass_();
    assertTrue(cl.hasSystem());
    assertTrue(cl.hasCode());
    assertTrue(cl.hasDisplay());
    assertEquals("http://genomics.ontoserver.csiro.au/clipi/ValueSet/EncounterClassValueSet", 
        cl.getSystem());
    assertEquals("RS", cl.getCode());
    assertEquals("research study", cl.getDisplay());
    assertTrue(enc.hasExtension());
    assertEquals(1, enc.getExtension().size());
    Extension ext = enc.getExtension().get(0);
    assertTrue(ext.hasUrl());
    assertEquals("http://myurl.com", ext.getUrl());
    assertTrue(ext.hasValue());
    Type t = ext.getValue();
    assertTrue(t instanceof Reference);
    Reference ref = (Reference) t;
    System.out.println(ref.getReference());
  }
  
  @Test
  public void testCreateClinicalResourcesFromRules() {
    RedmatchProject project = this.loadProject("tutorial");
    List<Row> rows = this.loadData("tutorial");

    Document rulesDocument = compiler.compile(project);
    assertNotNull(rulesDocument);
    final List<Annotation> compilationErrors = project.getIssues();
    for (Annotation ann : compilationErrors) {
      System.out.println(ann);
    }
    assertTrue(compilationErrors.isEmpty());
    
    List<Mapping> mappings = redmatchApi.generateMappings(project.getFields(),
            rulesDocument.getReferencedFields(project), rows);
    
    populateMappings(mappings);
    project.addMappings(mappings);
    
    try {
      Map<String, DomainResource> res = exporter.createClinicalResourcesFromRules(project, rulesDocument, rows);
    
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
      assertFalse(p1.getDeceasedBooleanType().booleanValue());
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
      testObservation(res, "obs1-1", "HP:0001558",
          "Decreased fetal movement", "POS");
      
      testObservation(res, "obs3-1", "HP:0031910",
          "Abnormal cranial nerve physiology", "POS");
      
      testObservation(res, "m-weak-1", "HP:0001324",
          "Muscle weakness", "POS");
      
      testObservation(res, "facial-1", "HP:0010628",
          "Facial palsy", "NEG");
      
      testObservation(res, "ptosis-1", "HP:0000508",
          "Ptosis", "POS");
      
      testObservation(res, "oph-1", "HP:0000602",
          "Ophthalmoplegia", "NEG");
      
      testObservation(res, "right-tricep-1", "HP:0001252",
          "Muscular hypotonia", "699996001", "Triceps brachii muscle and/or tendon structure",
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
      
      testObservation(res, "m-weak-2", "HP:0001324",
          "Muscle weakness", "NEG");
      
      testObservation(res, "left-bicep-2", "HP:0001276",
          "Hypertonia", "699956003", "Biceps brachii muscle and/or tendon structure", true);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
  
  /**
   * Test case for issue where a single resource is created despite the rules having a reference to
   * the patient.
   */
  @Test
  public void testIssue13() {
    RedmatchProject project = this.loadProject("bug", loadTestFile("rules_bug_patient_reference.rdm"));
    List<Row> rows = this.loadData("bug");

    Document rulesDocument = compiler.compile(project);
    assertNotNull(rulesDocument);
    final List<Annotation> compilationErrors = project.getIssues();
    for (Annotation ann : compilationErrors) {
      System.out.println(ann);
    }
    assertTrue(compilationErrors.isEmpty());

    try {
      Map<String, DomainResource> res = exporter.createClinicalResourcesFromRules(project, rulesDocument, rows);
      System.out.println(res.keySet());
      assertEquals(12, res.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Test case for functionality to reduce the precision of a date, for anonymisation purposes.
   */
  @Test
  public void testIssue3() {
    RedmatchProject project = this.loadProject("date");
    List<Row> rows = this.loadData("date");

    Document rulesDocument = compiler.compile(project);
    assertNotNull(rulesDocument);
    final List<Annotation> compilationErrors = project.getIssues();
    for (Annotation ann : compilationErrors) {
      System.out.println(ann);
    }

    printTokens(project.getRulesDocument());
    assertTrue(compilationErrors.isEmpty());

    try {
      Map<String, DomainResource> res = exporter.createClinicalResourcesFromRules(project, rulesDocument, rows);
      System.out.println(res.keySet());
      assertEquals(2, res.size());

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
      res.entrySet().stream().forEach(i -> {
        String key = i.getKey();
        DomainResource dr = i.getValue();
        assertTrue(dr instanceof Patient);
        Patient p = (Patient) dr;
        Date dob = p.getBirthDate();
        if (key.equals("p-1")) {
          // 2001
          try {
            Date expected = sdf.parse("2001");
            assertEquals(expected, dob);
          } catch (ParseException e) {
            e.printStackTrace();
            fail();
          }
        } else if (key.equals("p-2")) {
          // 2009
          try {
            Date expected = sdf.parse("2009");
            assertEquals(expected, dob);
          } catch (ParseException e) {
            e.printStackTrace();
            fail();
          }
        } else {
          fail("Unexpected key " + key);
        }
      });

    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  /**
   * Test case for bug where some transformations fail if values are not set.
   */
  @Test
  public void testIssue15() {
    RedmatchProject project = this.loadProject("date");
    List<Row> rows = this.loadData("date_missing");

    Document rulesDocument = compiler.compile(project);
    assertNotNull(rulesDocument);
    final List<Annotation> compilationErrors = project.getIssues();
    for (Annotation ann : compilationErrors) {
      System.out.println(ann);
    }
    assertTrue(compilationErrors.isEmpty());

    try {
      Map<String, DomainResource> res = exporter.createClinicalResourcesFromRules(project, rulesDocument, rows);
      System.out.println(res.keySet());
      assertEquals(3, res.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  private void testObservation(Map<String, DomainResource> res, String id, String code, String display,
                               String interpretation) {
    DomainResource dr  = res.get(id);
    assertTrue(res.containsKey(id));
    assertTrue(dr instanceof Observation);
    Observation obs = (Observation) dr;
    assertTrue(obs.hasStatus());
    assertEquals(ObservationStatus.FINAL, obs.getStatus());
    assertTrue(obs.hasCode());
    Coding cod = obs.getCode().getCodingFirstRep();
    assertEquals("http://purl.obolibrary.org/obo/hp.owl", cod.getSystem());
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
  
  private void testObservation(Map<String, DomainResource> res, String id,
                               String code, String display, String bodySiteCode,
                               String bodySiteDisplay, boolean lateralityLeft) {
    DomainResource dr  = res.get(id);
    assertTrue(res.containsKey(id));
    assertTrue(dr instanceof Observation);
    Observation obs = (Observation) dr;
    assertTrue(obs.hasStatus());
    assertEquals(ObservationStatus.FINAL, obs.getStatus());
    assertTrue(obs.hasCode());
    Coding cod = obs.getCode().getCodingFirstRep();
    assertEquals("http://purl.obolibrary.org/obo/hp.owl", cod.getSystem());
    assertEquals(code, cod.getCode());
    assertEquals(display, cod.getDisplay());
    assertTrue(obs.hasInterpretation());
    CodeableConcept inter = obs.getInterpretationFirstRep();
    assertTrue(inter.hasCoding());
    cod = inter.getCodingFirstRep();
    assertEquals("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation", 
        cod.getSystem());
    assertEquals("POS", cod.getCode());
    
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
  
  private void populateDataMappings (List<Mapping> mappings) {
    for (Mapping mapping : mappings) {
      String fieldId = mapping.getRedcapFieldId();
      if (fieldId.equals("pat_gene")) {
        String text = mapping.getText();
        if (text.equals("BRCA2")) {
          mapping.setTargetSystem("http://www.genenames.org/geneId");
          mapping.setTargetCode("HGNC:1101");
          mapping.setTargetDisplay("BRCA2");
        } else if (text.equals("BRCA1")) {
          mapping.setTargetSystem("http://www.genenames.org/geneId");
          mapping.setTargetCode("HGNC:1100");
          mapping.setTargetDisplay("BRCA1");
        }
        
      }
    }
  }
  
  private void populateMappings (List<Mapping> mappings) {
    for (Mapping mapping : mappings) {
      String fieldId = mapping.getRedcapFieldId();
      switch (fieldId) {
        case "pat_sex___1":
          mapping.setTargetCode("male");
          break;
        case "pat_sex___2":
          mapping.setTargetCode("female");
          break;
        case "dem_sex___1":
          mapping.setTargetSystem("http://snomed.info/sct");
          mapping.setTargetCode("248153007");
          mapping.setTargetDisplay("Male");
          break;
        case "dem_sex___2":
          mapping.setTargetSystem("http://snomed.info/sct");
          mapping.setTargetCode("248152002");
          mapping.setTargetDisplay("Female");
          break;
        case "phenotype___1":
          mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
          mapping.setTargetCode("HP:0001558");
          mapping.setTargetDisplay("Decreased fetal movement");
          break;
        case "phenotype___2":
          mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
          mapping.setTargetCode("HP:0001270");
          mapping.setTargetDisplay("Motor delay");
          break;
        case "phenotype___3":
          mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
          mapping.setTargetCode("HP:0031910");
          mapping.setTargetDisplay("Abnormal cranial nerve physiology");
          break;
        case "phenotype___4":
          mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
          mapping.setTargetCode("HP:0012587");
          mapping.setTargetDisplay("Macroscopic hematuria");
          break;
        case "m_weak":
          mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
          mapping.setTargetCode("HP:0001324");
          mapping.setTargetDisplay("Muscle weakness");
          break;
        case "facial":
          mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
          mapping.setTargetCode("HP:0010628");
          mapping.setTargetDisplay("Facial palsy");
          break;
        case "ptosis":
          mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
          mapping.setTargetCode("HP:0000508");
          mapping.setTargetDisplay("Ptosis");
          break;
        case "oph":
        case "mito_mw_ophthal":
          mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
          mapping.setTargetCode("HP:0000602");
          mapping.setTargetDisplay("Ophthalmoplegia");
          break;
        case "left_bicep___1":
        case "right_bicep___1":
        case "left_tricep___1":
        case "right_tricep___1":
          mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
          mapping.setTargetCode("HP:0001252");
          mapping.setTargetDisplay("Muscular hypotonia");
          break;
        case "left_bicep___3":
        case "right_tricep___3":
        case "right_bicep___3":
        case "left_tricep___3":
          mapping.setTargetSystem("http://purl.obolibrary.org/obo/hp.owl");
          mapping.setTargetCode("HP:0001276");
          mapping.setTargetDisplay("Hypertonia");
          break;
      }
    }
  }
}
