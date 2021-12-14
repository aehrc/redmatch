/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.exporter;

import au.csiro.redmatch.compiler.Document;
import au.csiro.redmatch.compiler.RedmatchCompiler;
import au.csiro.redmatch.model.Row;
import au.csiro.redmatch.util.FileUtils;
import au.csiro.redmatch.validation.RedmatchGrammarValidator;
import ca.uhn.fhir.context.FhirContext;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.Diagnostic;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link FhirExporter}.
 *
 * @author Alejandro Metke Jimenez
 */
public class FhirExporterTest {

  /** Logger. */
  private static final Log log = LogFactory.getLog(FhirExporterTest.class);

  private static RedmatchGrammarValidator validator;

  private static HapiReflectionHelper helper;

  private static final FhirContext ctx = FhirContext.forR4();

  private static final Gson gson = new Gson();

  @BeforeAll
  private static void init() {
    validator = new RedmatchGrammarValidator(gson, ctx);
    helper = new HapiReflectionHelper(ctx);
    helper.init();
  }

  @Test
  public void testTutorialCondition() {
    log.info("Running testTutorialCondition");
    String document = FileUtils.loadTextFileFromClassPath("testTutorialCondition.rdm");

    RedmatchCompiler compiler = new RedmatchCompiler(validator, gson);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    String json = FileUtils.loadTextFileFromClassPath("dataTutorial.json");
    List<Row> rows = parseData(json);
    FhirExporter exporter = new FhirExporter(doc, rows, helper);

    Map<String, DomainResource> res = exporter.transform(null, null);
    assertFalse(res.isEmpty());
  }

  @Test
  public void testAssignments() {
    log.info("Running testAssignments");
    String document = FileUtils.loadTextFileFromClassPath("testAssignments.rdm");

    RedmatchCompiler compiler = new RedmatchCompiler(validator, gson);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    String json = FileUtils.loadTextFileFromClassPath("dataTutorial.json");
    List<Row> rows = parseData(json);
    FhirExporter exporter = new FhirExporter(doc, rows, helper);

    Map<String, DomainResource> res = exporter.transform(null, null);
    assertFalse(res.isEmpty());

    // Check that the resources that were created are the correct ones
    Observation obs = (Observation) res.get("obs");
    assertNotNull(obs);
    assertEquals(Observation.ObservationStatus.FINAL, obs.getStatus());
    CodeableConcept code = obs.getCode();
    assertNotNull(code);
    assertEquals(1, code.getCoding().size());
    Coding coding = code.getCodingFirstRep();
    assertEquals("http://purl.obolibrary.org/obo/hp.owl", coding.getSystem());
    assertEquals("HP:0001558", coding.getCode());
    List<CodeableConcept> interpretations = obs.getInterpretation();
    assertEquals(2, interpretations.size());
    Set<String> interpretationCodes = new HashSet<>(Arrays.asList("A", "IE"));
    for (CodeableConcept interpretation : interpretations) {
      assertEquals(1, interpretation.getCoding().size());
      Coding interpretationCoding = interpretation.getCodingFirstRep();
      assertEquals("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
        interpretationCoding.getSystem());
      interpretationCodes.remove(interpretationCoding.getCode());
    }
    assertTrue(interpretationCodes.isEmpty());

    Observation obs2 = (Observation) res.get("obs2");
    assertNotNull(obs2);
    assertEquals(Observation.ObservationStatus.FINAL, obs2.getStatus());
    code = obs2.getCode();
    assertNotNull(code);
    assertEquals(1, code.getCoding().size());
    coding = code.getCodingFirstRep();
    assertEquals("http://purl.obolibrary.org/obo/hp.owl", coding.getSystem());
    assertEquals("HP:0001270", coding.getCode());
    interpretations = obs2.getInterpretation();
    assertEquals(2, interpretations.size());
    interpretationCodes = new HashSet<>(Arrays.asList("CAR", "POS"));
    for (CodeableConcept interpretation : interpretations) {
      assertEquals(1, interpretation.getCoding().size());
      Coding interpretationCoding = interpretation.getCodingFirstRep();
      assertEquals("http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation",
        interpretationCoding.getSystem());
      interpretationCodes.remove(interpretationCoding.getCode());
    }
    assertTrue(interpretationCodes.isEmpty());
    List<Reference> derivedFroms = obs2.getDerivedFrom();
    assertEquals(1, derivedFroms.size());
    Reference ref = derivedFroms.get(0);
    assertEquals("/Observation/obs", ref.getReference());

    Observation obs3 = (Observation) res.get("obs3");
    assertNotNull(obs3);
    assertEquals(Observation.ObservationStatus.FINAL, obs3.getStatus());
    code = obs3.getCode();
    assertNotNull(code);
    assertEquals(1, code.getCoding().size());
    coding = code.getCodingFirstRep();
    assertEquals("http://purl.obolibrary.org/obo/hp.owl", coding.getSystem());
    assertEquals("HP:0031910", coding.getCode());
    derivedFroms = obs3.getDerivedFrom();
    assertEquals(1, derivedFroms.size());
    ref = derivedFroms.get(0);
    assertEquals("/Observation/obs", ref.getReference());

    ValueSet vs = (ValueSet) res.get("vs");
    List<ValueSet.ConceptSetComponent> includes = vs.getCompose().getInclude();
    assertEquals(1, includes.size());
    ValueSet.ConceptSetComponent include = includes.get(0);
    List<ValueSet.ConceptSetFilterComponent> filters = include.getFilter();
    assertEquals(3, filters.size());

    ValueSet.ConceptSetFilterComponent firstFilter = filters.get(0);
    assertEquals("parent", firstFilter.getProperty());
    assertEquals(ValueSet.FilterOperator.ISA, firstFilter.getOp());
    assertEquals("True or false.", firstFilter.getValue());

    ValueSet.ConceptSetFilterComponent secondFilter = filters.get(1);
    assertEquals("deprecated", secondFilter.getProperty());
    assertEquals(ValueSet.FilterOperator.EQUAL, secondFilter.getOp());
    assertEquals("True or false.", secondFilter.getValue());

    ValueSet.ConceptSetFilterComponent thirdFilter = filters.get(2);
    assertEquals("root", thirdFilter.getProperty());
    assertEquals(ValueSet.FilterOperator.EQUAL, thirdFilter.getOp());
    assertEquals("True or false.", thirdFilter.getValue());
  }

  private List<Row> parseData(String data) {
    Type listType = new TypeToken<List<HashMap<String, String>>>() {}.getType();
    final List<Map<String, String>> rows = gson.fromJson(data, listType);

    final List<Row> res = new ArrayList<>();

    for (Map<String, String> row : rows) {
      Row entry = new Row();
      entry.setData(row);
      res.add(entry);
    }
    return res;
  }

  protected void printErrors(List<Diagnostic> errors) {
    for (Diagnostic error : errors) {
      log.info(error);
    }
  }

}
