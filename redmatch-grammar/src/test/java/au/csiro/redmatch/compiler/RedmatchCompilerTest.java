/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import au.csiro.redmatch.grammar.RedmatchLexer;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.terminology.TerminologyService;
import au.csiro.redmatch.util.FileUtils;
import ca.uhn.fhir.context.FhirContext;
import com.google.gson.Gson;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RedmatchCompiler}.
 *
 * @author Alejandro Metke-Jimenez
 */
public class RedmatchCompilerTest {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchCompilerTest.class);

  private static final FhirContext ctx = FhirContext.forR4();

  private static final Gson gson = new Gson();

  private final VersionedFhirPackage defaultFhirPackage = new VersionedFhirPackage("hl7.fhir.r4.core", "4.0.1");

  private final TerminologyService terminologyService = new TerminologyService(ctx, gson);

  @Test
  public void testComplexExtension() {
    log.info("Running testComplexExtension");
    String document = FileUtils.loadTextFileFromClassPath("testComplexExtension.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void testExtension() {
    log.info("Running testExtension");
    String document = FileUtils.loadTextFileFromClassPath("testExtension.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void testInvalidKeyword() {
    log.info("Running testInvalidKeyword");
    String document = FileUtils.loadTextFileFromClassPath("testInvalidKeyword.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testOneLiners() {
    log.info("Running testOneLiners");
    String document = FileUtils.loadTextFileFromClassPath("testOneLiners.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    List<Rule> rules = doc.getRules();
    assertEquals(2, rules.size());

    Rule r1 = rules.get(0);
    Condition c1 = r1.getCondition();
    assertTrue (c1 instanceof ConditionExpression);
    ConditionExpression ce1 = (ConditionExpression) c1;
    assertEquals(ConditionExpression.ConditionType.EXPRESSION, ce1.getConditionType());
    assertEquals(1, ce1.getIntValue().intValue());
    assertEquals(ConditionExpression.ConditionExpressionOperator.EQ, ce1.getOperator());
    assertEquals("pat_sex", ce1.getFieldId());
    Body b1 = r1.getBody();
    List<Resource> ress1 = b1.getResources();
    assertEquals(1, ress1.size());
    Resource res1 = ress1.get(0);
    assertEquals("Patient", res1.getResourceType());
    assertEquals("p", res1.getResourceId());
    List<AttributeValue> avs1 = res1.getResourceAttributeValues();
    assertEquals(1, avs1.size());
    AttributeValue av1 = avs1.get(0);
    List<Attribute> atts1 = av1.getAttributes();
    assertEquals(1, atts1.size());
    Attribute att1 = atts1.get(0);
    assertEquals("gender", att1.getName());
    Value val1 = av1.getValue();
    assertTrue(val1 instanceof CodeLiteralValue);
    CodeLiteralValue clv1 = (CodeLiteralValue) val1;
    assertEquals("male", clv1.getCode());

    Rule r2 = rules.get(1);
    Condition c2 = r2.getCondition();
    assertTrue (c2 instanceof ConditionExpression);
    ConditionExpression ce2 = (ConditionExpression) c2;
    assertEquals(ConditionExpression.ConditionType.EXPRESSION, ce2.getConditionType());
    assertEquals(2, ce2.getIntValue().intValue());
    assertEquals(ConditionExpression.ConditionExpressionOperator.EQ, ce2.getOperator());
    assertEquals("pat_sex", ce2.getFieldId());
    Body b2 = r2.getBody();
    List<Resource> ress2 = b2.getResources();
    assertEquals(1, ress2.size());
    Resource res2 = ress2.get(0);
    assertEquals("Patient", res2.getResourceType());
    assertEquals("p", res2.getResourceId());
    List<AttributeValue> avs2 = res2.getResourceAttributeValues();
    assertEquals(1, avs2.size());
    AttributeValue av2 = avs2.get(0);
    List<Attribute> atts2 = av2.getAttributes();
    assertEquals(1, atts2.size());
    Attribute att2 = atts2.get(0);
    assertEquals("gender", att2.getName());
    Value val2 = av2.getValue();
    assertTrue(val2 instanceof CodeLiteralValue);
    CodeLiteralValue clv2 = (CodeLiteralValue) val2;
    assertEquals("female", clv2.getCode());
  }

  @Test
  public void testInvalidOneLiner() {
    log.info("Running testInvalidOneLiner");
    String document = FileUtils.loadTextFileFromClassPath("testInvalidOneLiner.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testLoincConceptLiteral() {
    log.info("Running testLoincConceptLiteral");
    String document = FileUtils.loadTextFileFromClassPath("testLoincConceptLiteral.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    assertNotNull(doc.getRules());
    assertEquals(1, doc.getRules().size());
  }

  @Test
  public void testComplexCondition() {
    log.info("Running testComplexCondition");
    String document = FileUtils.loadTextFileFromClassPath("testComplexCondition.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    assertNotNull(doc.getRules());
    assertEquals(1, doc.getRules().size());
    Rule r = doc.getRules().get(0);

    Condition c = r.getCondition();
    assertTrue(c instanceof ConditionNode);
  }

  @Test
  public void testEvenMoreComplexCondition() {
    log.info("Running testEvenMoreComplexCondition");
    String document = FileUtils.loadTextFileFromClassPath("testEvenMoreComplexCondition.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    assertNotNull(doc.getRules());
    assertEquals(1, doc.getRules().size());
    Rule r = doc.getRules().get(0);

    Condition c = r.getCondition();
    assertTrue(c instanceof ConditionNode);
  }

  @Test
  public void testComplexConditionWithParenthesis() {
    log.info("Running testComplexConditionWithParenthesis");
    String document = FileUtils.loadTextFileFromClassPath("testComplexConditionWithParenthesis.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    assertNotNull(doc.getRules());
    assertEquals(1, doc.getRules().size());
    Rule r = doc.getRules().get(0);

    Condition c = r.getCondition();
    assertTrue(c instanceof ConditionNode);
  }

  @Test
  public void testInvalidField() {
    log.info("Running testInvalidField");
    String document = FileUtils.loadTextFileFromClassPath("testInvalidField.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testId() {
    log.info("Running testId");
    String document = FileUtils.loadTextFileFromClassPath("testId.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void testInvalidId() {
    log.info("Running testInvalidId");
    String document = FileUtils.loadTextFileFromClassPath("testInvalidId.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testInvalidId2() {
    log.info("Running testInvalidId2");
    String document = FileUtils.loadTextFileFromClassPath("testInvalidId2.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testListExplicit() {
    log.info("Running testListExplicit");
    String document = FileUtils.loadTextFileFromClassPath("testListExplicit.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    // Test it is a single rule
    List<Rule> rules = doc.getRules();
    assertEquals(1, rules.size());
    Rule r = rules.get(0);

    // Test the condition
    Condition c = r.getCondition();
    assertTrue(c instanceof ConditionExpression);
    ConditionExpression ce = (ConditionExpression) c;
    assertEquals(ConditionExpression.ConditionType.TRUE, ce.getConditionType());

    // Test body - should only have resources
    Body b = r.getBody();
    List<Resource> resources = b.getResources();
    assertEquals(1, resources.size());
    assertTrue(b.getRules().isEmpty());

    Resource resource = resources.get(0);
    assertEquals("p", resource.getResourceId());
    assertEquals("Patient", resource.getResourceType());

    List<AttributeValue> attrsVals = resource.getResourceAttributeValues();
    assertEquals(1, attrsVals.size());

    AttributeValue identValue = attrsVals.get(0);
    List<Attribute> attrs = identValue.getAttributes();
    Attribute ident = attrs.get(0);
    assertEquals("identifier", ident.getName());
    assertEquals(Integer.valueOf(0), ident.getAttributeIndex());

    Attribute val = attrs.get(1);
    assertEquals("value", val.getName());
    assertNull(val.getAttributeIndex());
  }

  @Test
  public void testListImplicit() {
    log.info("Running testListImplicit");
    String document = FileUtils.loadTextFileFromClassPath("testListImplicit.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    // Test it is a single rule
    List<Rule> rules = doc.getRules();
    assertEquals(1, rules.size());
    Rule r = rules.get(0);

    // Test the condition
    Condition c = r.getCondition();
    assertTrue(c instanceof ConditionExpression);
    ConditionExpression ce = (ConditionExpression) c;
    assertEquals(ConditionExpression.ConditionType.TRUE, ce.getConditionType());

    // Test body - should only have resources
    Body b = r.getBody();
    List<Resource> resources = b.getResources();
    assertEquals(1, resources.size());
    assertTrue(b.getRules().isEmpty());

    Resource resource = resources.get(0);
    assertEquals("p", resource.getResourceId());
    assertEquals("Patient", resource.getResourceType());

    List<AttributeValue> attrsVals = resource.getResourceAttributeValues();
    assertEquals(1, attrsVals.size());

    AttributeValue identValue = attrsVals.get(0);
    List<Attribute> attrs = identValue.getAttributes();
    Attribute ident = attrs.get(0);
    assertEquals("identifier", ident.getName());
    assertNull(ident.getAttributeIndex());

    Attribute val = attrs.get(1);
    assertEquals("value", val.getName());
    assertNull(val.getAttributeIndex());
  }

  @Test
  public void testWrongAttribute() {
    log.info("Running testWrongAttribute");
    String document = FileUtils.loadTextFileFromClassPath("testWrongAttribute.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testTutorialPatient() {
    log.info("Running testTutorialPatient");
    String document = FileUtils.loadTextFileFromClassPath("testTutorialPatient.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    // Test it is a single rule
    List<Rule> rules = doc.getRules();
    assertEquals(1, rules.size());
    Rule r = rules.get(0);

    // Test the condition
    Condition c = r.getCondition();
    assertTrue(c instanceof ConditionExpression);
    ConditionExpression ce = (ConditionExpression) c;
    assertEquals(ConditionExpression.ConditionType.TRUE, ce.getConditionType());

    // Test body - should only have resources
    Body b = r.getBody();
    List<Resource> resources = b.getResources();
    assertEquals(1, resources.size());
    assertTrue(b.getRules().isEmpty());

    Resource resource = resources.get(0);
    assertEquals("p", resource.getResourceId());
    assertEquals("Patient", resource.getResourceType());

    // Test attributes and values
    List<AttributeValue> attrsVals = resource.getResourceAttributeValues();
    assertEquals(4, attrsVals.size());

    // identifier.type = CONCEPT_LITERAL(http://hl7.org/fhir/v2/0203|MC)
    testConceptLiteralAttributeValue(attrsVals.get(0), new String[] {"identifier", "type"},
      new int[] {-1, -1});

    // identifier.type.text = "Medicare Number"
    testStringAttributeValue(attrsVals.get(1), new String[] {"identifier", "type", "text"},
      new int[] {-1, -1, -1}, "Medicare Number");

    // identifier.system = "http://ns.electronichealth.net.au/id/medicare-number"
    testStringAttributeValue(attrsVals.get(2), new String[] {"identifier", "system"},
      new int[] {-1, -1}, "http://ns.electronichealth.net.au/id/medicare-number");

    // identifier.value = VALUE(pat_medicare)
    testFieldBasedAttributeValue(attrsVals.get(3), new String[] {"identifier", "value"},
      new int[] {-1, -1}, "pat_medicare", FieldValue.class);
  }

  @Test
  public void testMissingResource() {
    log.info("Running testMissingResource");
    String document = FileUtils.loadTextFileFromClassPath("testMissingResource.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testTutorialCondition() {
    log.info("Running testTutorialCondition");
    String document = FileUtils.loadTextFileFromClassPath("testTutorialCondition.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    // Test there are two rules
    List<Rule> rules = doc.getRules();
    assertEquals(2, rules.size());
    Rule r = rules.get(1);

    // Test the condition
    testConditionExpression(r);

    // Test body - should have two nested rules (repeats clause is evaluated)
    Body b = r.getBody();
    List<Resource> resources = b.getResources();
    List<Rule> nestedRules = b.getRules();
    assertTrue(resources.isEmpty());
    assertEquals(2, nestedRules.size());

    Rule nestedRule = nestedRules.get(0);
    testConditionExpression(nestedRule, "dx_1");

    // Test nested body
    b = nestedRule.getBody();
    List<Resource> nestedResources = b.getResources();
    assertEquals(1, nestedResources.size());
    assertTrue(b.getRules().isEmpty());

    Resource resource = nestedResources.get(0);
    assertEquals("c1", resource.getResourceId());
    assertEquals("Condition", resource.getResourceType());

    // code.text = VALUE(dx_text_${x})
    AttributeValue av = resource.getResourceAttributeValues().get(0);
    testFieldBasedAttributeValue(av, new String[] {"code", "text"},  new int[] {-1, -1}, "dx_text_1", FieldValue.class);

    // subject = REF(Patient<p>)
    av = resource.getResourceAttributeValues().get(1);
    testReferenceAttributeValue(av, new String[] {"subject"},  new int[] {-1});

    // Test else body
    b = nestedRule.getElseBody();
    nestedResources = b.getResources();
    assertEquals(1, nestedResources.size());
    assertTrue(b.getRules().isEmpty());

    resource = nestedResources.get(0);
    assertEquals("c1", resource.getResourceId());
    assertEquals("Condition", resource.getResourceType());

    // code = CONCEPT(dx_${x})
    av = resource.getResourceAttributeValues().get(0);
    testFieldBasedAttributeValue(av, new String[] {"code"},  new int[] {-1}, "dx_1", ConceptValue.class);

    // subject = REF(Patient<p>)
    av = resource.getResourceAttributeValues().get(1);
    testReferenceAttributeValue(av, new String[] {"subject"},  new int[] {-1});

    nestedRule = nestedRules.get(1);
    testConditionExpression(nestedRule, "dx_2");

    // Test nested body
    b = nestedRule.getBody();
    nestedResources = b.getResources();
    assertEquals(1, nestedResources.size());
    assertTrue(b.getRules().isEmpty());

    resource = nestedResources.get(0);
    assertEquals("c2", resource.getResourceId());
    assertEquals("Condition", resource.getResourceType());

    // code.text = VALUE(dx_text_${x})
    av = resource.getResourceAttributeValues().get(0);
    testFieldBasedAttributeValue(av, new String[] {"code", "text"},  new int[] {-1, -1}, "dx_text_2", FieldValue.class);

    // subject = REF(Patient<p>)
    av = resource.getResourceAttributeValues().get(1);
    testReferenceAttributeValue(av, new String[] {"subject"},  new int[] {-1});

    // Test else body
    b = nestedRule.getElseBody();
    nestedResources = b.getResources();
    assertEquals(1, nestedResources.size());
    assertTrue(b.getRules().isEmpty());

    resource = nestedResources.get(0);
    assertEquals("c2", resource.getResourceId());
    assertEquals("Condition", resource.getResourceType());

    // code = CONCEPT(dx_${x})
    av = resource.getResourceAttributeValues().get(0);
    testFieldBasedAttributeValue(av, new String[] {"code"},  new int[] {-1}, "dx_2", ConceptValue.class);

    // subject = REF(Patient<p>)
    av = resource.getResourceAttributeValues().get(1);
    testReferenceAttributeValue(av, new String[] {"subject"},  new int[] {-1});
  }

  @Test
  public void testMappings() {
    log.info("Running testMappings");
    String document = FileUtils.loadTextFileFromClassPath("testMappings.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());

    Map<String, Mapping> mappings = doc.getMappings();
    assertEquals(6, mappings.size());
    Mapping patSex1 = mappings.get("pat_sex___1");
    testMapping(patSex1, "http://snomed.info/sct", "248153007", "Male");
    Mapping patSex2 = mappings.get("pat_sex___2");
    testMapping(patSex2, "http://snomed.info/sct", "248152002", "Female");
    Mapping phenotype1 = mappings.get("phenotype___1");
    testMapping(phenotype1, "http://purl.obolibrary.org/obo/hp.fhir", "HP:0000602", "Ophthalmoplegia");
    Mapping phenotype2 = mappings.get("phenotype___2");
    testMapping(phenotype2, "http://purl.obolibrary.org/obo/hp.fhir", "HP:0000602", "Ophthalmoplegia");
    Mapping phenotype3 = mappings.get("phenotype___3");
    testMapping(phenotype3, "http://purl.obolibrary.org/obo/hp.fhir", "HP:0000602", "Ophthalmoplegia");
    Mapping phenotype4 = mappings.get("phenotype___4");
    testMapping(phenotype4, "http://purl.obolibrary.org/obo/hp.fhir", "HP:0000602", "Ophthalmoplegia");
  }

  @Test
  public void testMappingsMissingAlias() {
    log.info("Running testMappingsMissingAlias");
    String document = FileUtils.loadTextFileFromClassPath("testMappingsMissingAlias.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testMappingsUnnecessaryMapping() {
    log.info("Running testMappingsUnnecessaryMapping");
    String document = FileUtils.loadTextFileFromClassPath("testMappingsUnnecessaryMapping.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> warnings = doc.getDiagnostics();
    printErrors(warnings);
    assertFalse(warnings.isEmpty());

    assertEquals(1, warnings.size());
    Diagnostic warning = warnings.get(0);
    assertEquals(DiagnosticSeverity.Warning, warning.getSeverity());
  }

  @Test
  public void testMappingsFieldDoesNotExist() {
    log.info("Running testMappingsFieldDoesNotExist");
    String document = FileUtils.loadTextFileFromClassPath("testMappingsFieldDoesNotExist.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertFalse(errors.isEmpty());

    assertEquals(1, errors.size());
    Diagnostic error = errors.get(0);
    assertEquals(DiagnosticSeverity.Error, error.getSeverity());
  }

  @Test
  public void testOldRules() {
    log.info("Running testOldRules");
    String document = FileUtils.loadTextFileFromClassPath("testOldRules.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }

  @Test
  public void testReferenceValidation() {
    log.info("Running testReferenceValidation");
    String document = FileUtils.loadTextFileFromClassPath("testReferenceValidation.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.stream().anyMatch(e -> e.getSeverity().equals(DiagnosticSeverity.Error)));
  }

  @Test
  public void testTarget() {
    log.info("Running testTarget");
    String document = FileUtils.loadTextFileFromClassPath("testTarget.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.isEmpty());
  }

  @Test
  public void testInvalidTarget() {
    log.info("Running testInvalidTarget");
    String document = FileUtils.loadTextFileFromClassPath("testInvalidTarget.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.stream().anyMatch(e -> e.getSeverity().equals(DiagnosticSeverity.Error)));
  }

  @Test
  public void testTargetInvalidReference() {
    log.info("Running testTargetInvalidReference");
    String document = FileUtils.loadTextFileFromClassPath("testTargetInvalidReference.rdm");
    printTokens(document);

    RedmatchCompiler compiler = new RedmatchCompiler(gson, terminologyService, defaultFhirPackage);
    Document doc = compiler.compile(document);
    List<Diagnostic> errors = doc.getDiagnostics();
    printErrors(errors);
    assertTrue(errors.stream().anyMatch(e -> e.getSeverity().equals(DiagnosticSeverity.Error)));
  }

  private void testMapping(Mapping m, String system, String code, String display) {
    assertNotNull(m.getTarget());
    assertEquals(system, m.getTarget().getSystem());
    assertEquals(code, m.getTarget().getCode());
    assertEquals(display, m.getTarget().getDisplay());
  }

  protected void printTokens(String rule) {
    System.out.println("TOKENS:");
    final Lexer lexer = new RedmatchLexer(CharStreams.fromString(rule));
    for (Token tok : lexer.getAllTokens()) {
      System.out.println(tok);
    }
  }

  protected void printErrors(List<Diagnostic> errors) {
    for (Diagnostic error : errors) {
      log.info(error);
    }
  }

  private void testFieldBasedAttributeValue(AttributeValue av, String[] expectedPath, int[] expectedIndexes,
                                            String expectedValue, Class<? extends FieldBasedValue> clazz) {
    List<Attribute> attrs = av.getAttributes();
    assertEquals (expectedPath.length, attrs.size());

    for (int i = 0; i < attrs.size(); i++) {
      Attribute id = attrs.get(i);
      assertEquals(expectedPath[i], id.getName());
      if (expectedIndexes[i] == -1) {
        assertNull(id.getAttributeIndex());
      } else {
        assertEquals(expectedIndexes[i], id.getAttributeIndex().intValue());
      }
    }

    Value val = av.getValue();
    assertTrue(val instanceof FieldBasedValue);
    assertEquals(expectedValue, ((FieldBasedValue) val).getFieldId());
    assertEquals(clazz, val.getClass());
  }

  private void testStringAttributeValue(AttributeValue av, String[] expectedPath, int[] expectedIndexes,
                                        String expectedValue) {
    List<Attribute> attrs = av.getAttributes();
    assertEquals (expectedPath.length, attrs.size());

    for (int i = 0; i < attrs.size(); i++) {
      Attribute id = attrs.get(i);
      assertEquals(expectedPath[i], id.getName());
      if (expectedIndexes[i] == -1) {
        assertNull(id.getAttributeIndex());
      } else {
        assertEquals(expectedIndexes[i], id.getAttributeIndex().intValue());
      }
    }

    Value val = av.getValue();
    assertTrue(val instanceof StringValue);
    assertEquals(expectedValue, ((StringValue) val).getStringValue());
  }

  private void testConceptLiteralAttributeValue(AttributeValue av, String[] expectedPath, int[] expectedIndexes) {
    List<Attribute> attrs = av.getAttributes();
    assertEquals (expectedPath.length, attrs.size());

    for (int i = 0; i < attrs.size(); i++) {
      Attribute id = attrs.get(i);
      assertEquals(expectedPath[i], id.getName());
      if (expectedIndexes[i] == -1) {
        assertNull(id.getAttributeIndex());
      } else {
        assertEquals(expectedIndexes[i], id.getAttributeIndex().intValue());
      }
    }

    Value val = av.getValue();
    assertTrue(val instanceof ConceptLiteralValue);
    ConceptLiteralValue clv = (ConceptLiteralValue) val;
    assertEquals("http://hl7.org/fhir/v2/0203", clv.getSystem());
    assertEquals("MC", clv.getCode());
    assertNull(clv.getDisplay());
  }

  private void testReferenceAttributeValue(AttributeValue av, String[] expectedPath,
                                           int[] expectedIndexes) {
    List<Attribute> attrs = av.getAttributes();
    assertEquals (expectedPath.length, attrs.size());

    for (int i = 0; i < attrs.size(); i++) {
      Attribute id = attrs.get(i);
      assertEquals(expectedPath[i], id.getName());
      if (expectedIndexes[i] == -1) {
        assertNull(id.getAttributeIndex());
      } else {
        assertEquals(expectedIndexes[i], id.getAttributeIndex().intValue());
      }
    }

    Value val = av.getValue();
    assertTrue(val instanceof ReferenceValue);
    ReferenceValue rv = (ReferenceValue) val;
    assertEquals("Patient", rv.getResourceType());
    assertEquals("p", rv.getResourceId());
  }

  private void testConditionExpression(Rule r) {
    Condition c = r.getCondition();
    assertTrue(c instanceof ConditionExpression);
    ConditionExpression ce = (ConditionExpression) c;
    assertEquals(ConditionExpression.ConditionType.EXPRESSION, ce.getConditionType());
    assertEquals("dx_num", ce.getFieldId());
    assertEquals(ConditionExpression.ConditionExpressionOperator.GT, ce.getOperator());
    assertEquals(0, ce.getIntValue().intValue());
  }

  private void testConditionExpression(Rule r, String fieldId) {
    Condition c = r.getCondition();
    assertTrue(c instanceof ConditionExpression);
    ConditionExpression ce = (ConditionExpression) c;
    assertEquals(ConditionExpression.ConditionType.EXPRESSION, ce.getConditionType());
    assertEquals(fieldId, ce.getFieldId());
    assertEquals(ConditionExpression.ConditionExpressionOperator.EQ, ce.getOperator());
    assertEquals("NOT_FOUND", ce.getStringValue());
  }

}
