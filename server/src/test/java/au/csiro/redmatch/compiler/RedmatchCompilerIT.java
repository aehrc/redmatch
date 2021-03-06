/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import au.csiro.redmatch.model.RedmatchProject;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import au.csiro.redmatch.AbstractRedmatchTest;
import au.csiro.redmatch.client.ITerminologyClient;
import au.csiro.redmatch.grammar.RedmatchLexer;
import au.csiro.redmatch.model.Annotation;
import au.csiro.redmatch.model.grammar.redmatch.Attribute;
import au.csiro.redmatch.model.grammar.redmatch.AttributeValue;
import au.csiro.redmatch.model.grammar.redmatch.Body;
import au.csiro.redmatch.model.grammar.redmatch.CodeLiteralValue;
import au.csiro.redmatch.model.grammar.redmatch.ConceptLiteralValue;
import au.csiro.redmatch.model.grammar.redmatch.ConceptValue;
import au.csiro.redmatch.model.grammar.redmatch.Condition;
import au.csiro.redmatch.model.grammar.redmatch.ConditionExpression;
import au.csiro.redmatch.model.grammar.redmatch.ConditionExpression.ConditionExpressionOperator;
import au.csiro.redmatch.model.grammar.redmatch.ConditionExpression.ConditionType;
import au.csiro.redmatch.model.grammar.redmatch.ConditionNode;
import au.csiro.redmatch.model.grammar.redmatch.Document;
import au.csiro.redmatch.model.grammar.redmatch.FieldBasedValue;
import au.csiro.redmatch.model.grammar.redmatch.FieldValue;
import au.csiro.redmatch.model.grammar.redmatch.ReferenceValue;
import au.csiro.redmatch.model.grammar.redmatch.Resource;
import au.csiro.redmatch.model.grammar.redmatch.Rule;
import au.csiro.redmatch.model.grammar.redmatch.StringValue;
import au.csiro.redmatch.model.grammar.redmatch.Value;
import au.csiro.redmatch.validation.MockTerminolgyClient;

/**
 * Redmatch compiler unit tests.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@ExtendWith(SpringExtension.class)
@PropertySource("classpath:application.properties")
@SpringBootTest
public class RedmatchCompilerIT extends AbstractRedmatchTest {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchCompilerIT.class);
  
  @Autowired
  private RedmatchCompiler compiler;
  
  private final ITerminologyClient mockTerminologyServer = new MockTerminolgyClient();
  
  @BeforeEach
  public void hookMock() {
    log.info("Setting mock terminology server");
    compiler.getValidator().setClient(mockTerminologyServer);
  }
  
  @Test
  public void testComplexExtension() {
    String rule = "TRUE {\n" + 
        "  ResearchStudy<rstud> :\n" + 
        "    * extension.valueQuantity.extension.valueQuantity.extension.valueQuantity."
        + "system = 'http://mysystem.com'" + 
        "}";
    
    printTokens(rule);
    final RedmatchProject project = loadProject("tutorial", rule);
    compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertTrue(errors.isEmpty());
  }
  
  @Test
  public void testExtension() {
    String rule = "TRUE {\n" + 
        "  ResearchStudy<rstud> :\n" + 
        "    * identifier.type = CONCEPT_LITERAL(http://genomics.ontoserver.csiro.au/clipi/" + 
        "CodeSystem/IdentifierType|RSI|'Research study identifier')\n" + 
        "    * identifier.system = 'http://www.australiangenomics.org.au/id/research-study'\n" + 
        "    * identifier.value = 'mito'\n" + 
        "}\n" + 
        "\n" + 
        "TRUE {\n" + 
        "  Encounter<enc> :\n" + 
        "    * extension[0].url = "
        + "'http://hl7.org/fhir/StructureDefinition/workflow-researchStudy'\n" + 
        "    * extension[0].valueReference = REF(ResearchStudy<rstud>)\n" + 
        "}";
    
    printTokens(rule);
    final RedmatchProject project = loadProject("tutorial", rule);
    compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertTrue(errors.isEmpty());
  }
  
  @Test
  public void testInvalidKeyword() {
    String rule = "TRUE { \n" + 
        "  Patient<p-1>: \n" + 
        "    *identifier[0].value = VALUE(record_id) \n" + 
        "}\n" + 
        "\n" + 
        "BALUE(pat_sex_xy) = 21 { \n" + 
        "  Patient<p>: \n" + 
        "    *gender = CODE_LITERAL(male) \n" + 
        "}";
    
    printTokens(rule);
    final RedmatchProject project = loadProject("tutorial", rule);
    compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }
  
  /**
   * Unit test for FHIR-39.
   */
  @Test
  public void testValidOneLiners() {
    String rule = "VALUE(pat_sex) = 1 { Patient<p>: *gender = CODE_LITERAL(male) }\n" + 
        "VALUE(pat_sex) = 2 { Patient<p>: *gender = CODE_LITERAL(female) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    Document doc = compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertTrue(errors.isEmpty());
    
    List<Rule> rules = doc.getRules();
    assertEquals(2, rules.size());
    
    Rule r1 = rules.get(0);
    Condition c1 = r1.getCondition();
    assertTrue (c1 instanceof ConditionExpression);
    ConditionExpression ce1 = (ConditionExpression) c1;
    assertEquals(ConditionType.EXPRESSION, ce1.getConditionType());
    assertEquals(1, ce1.getIntValue().intValue());
    assertEquals(ConditionExpressionOperator.EQ, ce1.getOperator());
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
    assertEquals(ConditionType.EXPRESSION, ce2.getConditionType());
    assertEquals(2, ce2.getIntValue().intValue());
    assertEquals(ConditionExpressionOperator.EQ, ce2.getOperator());
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
    String rule = "TTRUE { Patient<p>: *gender = CODE_LITERAL(male) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }
  
  @Test
  public void testLoincConceptLiteral() {
    String rule = "TRUE { Observation<o>: *code = CONCEPT_LITERAL(http://loinc.org|48018-6) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    Document doc = compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertTrue(errors.isEmpty());
    
    assertNotNull(doc.getRules());
    assertEquals(1, doc.getRules().size());
  }
  
  @Test
  public void testComplexCondition() {
    String rule = "VALUE(facial) = 1 & VALUE(ptosis) = 1 "
        + "{ Patient<p>: *identifier[0].value = VALUE(record_id) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    Document doc = compiler.compile(project);
    List<Annotation> errors = project.getIssues();
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
    String rule = "VALUE(facial) = 1 & VALUE(ptosis) = 1 | VALUE(oph) = 2"
        + "{ Patient<p>: *identifier[0].value = VALUE(record_id) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    Document doc = compiler.compile(project);
    List<Annotation> errors = project.getIssues();
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
    String rule = "VALUE(facial) = 1 & (VALUE(ptosis) = 1 | VALUE(oph) = 2)"
        + "{ Patient<p>: *identifier[0].value = VALUE(record_id) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    Document doc = compiler.compile(project);
    List<Annotation> errors = project.getIssues();
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
    final String rule = "TRUE { Patient<p>: *identifier[0].value = VALUE(stud_num) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }
  
  @Test
  public void testValidId() {
    final String rule = "TRUE { Patient<p-1>: *identifier[0].value = VALUE(record_id) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertTrue(errors.isEmpty());
  }
  
  /**
   * Unit test for FHIR-16, FHIR ids are not being validated.
   */
  @Test
  public void testInvalidId() {
    final String rule = "TRUE { Patient<p_1>: *identifier[0].value = VALUE(record_id) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }
  
  @Test
  public void testInvalidId2() {
    final String rule = "// Might lose some detail here if other is specified - should migrate to "
        + "Ontoserver plugin\nVALUE(m_weak) = 1 { "
        + "Observation<mito_cd_atax>: *status = CODE_LITERAL(final) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertFalse(errors.isEmpty());
  }
  
  @Test
  public void testListExplicit() {
    final String rule = "TRUE { Patient<p>: *identifier[0].value = VALUE(record_id) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    Document doc = compiler.compile(project);
    List<Annotation> errors = project.getIssues();
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
    assertEquals(ConditionType.TRUE, ce.getConditionType());
    
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
    final String rule = "TRUE { Patient<p>: *identifier.value = VALUE(record_id) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    Document doc = compiler.compile(project);
    List<Annotation> errors = project.getIssues();
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
    assertEquals(ConditionType.TRUE, ce.getConditionType());
    
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
    final String rule = "TRUE { Patient<p>: *identifiers.value = VALUE(record_id) }";
    
    printTokens(rule);
    
    final RedmatchProject project = loadProject("tutorial", rule);
    compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    
    assertFalse(errors.isEmpty());
  }
  
  @Test
  public void testTutorialPatient() {
    final String rule = "TRUE { \n" +
                "  Patient<p>:\n" +
                "    *identifier.type = CONCEPT_LITERAL(http://hl7.org/fhir/v2/0203|MC)\n" +
                "    *identifier.type.text = 'Medicare Number'\n" +
                "    *identifier.system = 'http://ns.electronichealth.net.au/id/medicare-number'\n" +
                "    *identifier.value = VALUE(pat_medicare)\n" +
                "}";
    
    printTokens(rule);
    
    RedmatchProject project = this.loadProject("tutorial", rule);
    Document doc = compiler.compile(project);
    List<Annotation> errors = project.getIssues();
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
    assertEquals(ConditionType.TRUE, ce.getConditionType());
    
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
  public void testTutorialCondition() {
    final String rule = 
        "VALUE(dx_num) > 0 {\n" + 
        "  REPEAT(1..2: x)\n" + 
        "  VALUE(dx_${x}) = 'NOT_FOUND' {\n" +
        "    // No code was found so we use the free text\n" + 
        "    Condition<c${x}>: \n" + 
        "      *code.text = VALUE(dx_text_${x})\n" + 
        "      *subject = REF(Patient<p>)\n" + 
        "  } ELSE {\n" + 
        "    // We use the code selected using the terminology server\n" + 
        "    Condition<c${x}>: \n" + 
        "      *code = CONCEPT(dx_${x})\n" + 
        "      *subject = REF(Patient<p>)\n" + 
        "  }\n" + 
        "}";
    
    printTokens(rule);
    
    RedmatchProject project = this.loadProject("tutorial", rule);
    Document doc = compiler.compile(project);
    List<Annotation> errors = project.getIssues();
    printErrors(errors);
    assertTrue(errors.isEmpty());
    
    // Test it is a single rule
    List<Rule> rules = doc.getRules();
    assertEquals(1, rules.size());
    Rule r = rules.get(0);
    
    // Test the condition
    testConditionExpression(r
    );
    
    // Test body - should have two nested rules (repeats clause is evaluated)
    Body b = r.getBody();
    List<Resource> resources = b.getResources();
    List<Rule> nestedRules = b.getRules();
    assertTrue(resources.isEmpty());
    assertEquals(2, nestedRules.size());
    
    Rule nestedRule = nestedRules.get(0);
    testConditionExpression(nestedRule, "dx_1"
    );
    
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
    testFieldBasedAttributeValue(av, new String[] {"code", "text"},  new int[] {-1, -1}, 
        "dx_text_1", FieldValue.class);
    
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
    testFieldBasedAttributeValue(av, new String[] {"code"},  new int[] {-1}, 
        "dx_1", ConceptValue.class);
    
    // subject = REF(Patient<p>)
    av = resource.getResourceAttributeValues().get(1);
    testReferenceAttributeValue(av, new String[] {"subject"},  new int[] {-1});
    
    nestedRule = nestedRules.get(1);
    testConditionExpression(nestedRule, "dx_2"
    );
    
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
    testFieldBasedAttributeValue(av, new String[] {"code", "text"},  new int[] {-1, -1}, 
        "dx_text_2", FieldValue.class);
    
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
    testFieldBasedAttributeValue(av, new String[] {"code"},  new int[] {-1}, 
        "dx_2", ConceptValue.class);
    
    // subject = REF(Patient<p>)
    av = resource.getResourceAttributeValues().get(1);
    testReferenceAttributeValue(av, new String[] {"subject"},  new int[] {-1});
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
    assertEquals(ConditionType.EXPRESSION, ce.getConditionType());
    assertEquals("dx_num", ce.getFieldId());
    assertEquals(ConditionExpressionOperator.GT, ce.getOperator());
    assertEquals(0, ce.getIntValue().intValue());
  }
  
  private void testConditionExpression(Rule r, String fieldId) {
    Condition c = r.getCondition();
    assertTrue(c instanceof ConditionExpression);
    ConditionExpression ce = (ConditionExpression) c;
    assertEquals(ConditionType.EXPRESSION, ce.getConditionType());
    assertEquals(fieldId, ce.getFieldId());
    assertEquals(ConditionExpressionOperator.EQ, ce.getOperator());
    assertEquals("NOT_FOUND", ce.getStringValue());
  }

}
