/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import au.csiro.redmatch.grammar.RedmatchLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.SemanticTokens;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link SemanticTokeniser} class.
 */
public class SemanticTokeniserTest {
  @Test
  public void testTokenise() {
    String rule = "SCHEMA: 'src/test/resources/schema.json' (REDCAP)\n" +
      "\n" +
      "RULES: {\n" +
      "  TRUE { Patient<p-1>: *identifier[0].value = VALUE(record_id) }\n" +
      "}";
    printTokens(rule);

    // Encoded
    List<Integer> expectedTokens = Arrays.asList(
      0, 0, 6, 0, 0,
      0, 8, 32, 3, 0,
      0, 34, 6, 6, 0,
      2, 0, 5, 0, 0,
      1, 2, 4, 0, 0,
      0, 7, 7, 7, 0,
      0, 7, 1, 4, 0,
      0, 1, 3, 7, 0,
      0, 3, 1, 4, 0,
      0, 3, 1, 5, 0,
      0, 12, 1, 5, 0,
      0, 2, 1, 3, 0,
      0, 7, 1, 2, 0,
      0, 2, 5, 0, 0,
      0, 6, 9, 7, 0
    );

    SemanticTokens semanticTokens = SemanticTokeniser.tokenise(rule);
    List<Integer> actualTokens = semanticTokens.getData();
    assertEquals(expectedTokens, actualTokens);
  }

  @Test
  public void testTokeniseInvalid() {
    String rule = "SCHEMA: 'src/test/resources/schema.json' (REDCAP)\n" +
      "\n" +
      "BALUE!";
    printTokens(rule);

    // Encoded
    List<Integer> expectedTokens = Arrays.asList(
      0, 0, 6, 0, 0,
      0, 8, 32, 3, 0,
      0, 34, 6, 6, 0,
      2, 0, 5, 6, 0
    );

    SemanticTokens semanticTokens = SemanticTokeniser.tokenise(rule);
    List<Integer> actualTokens = semanticTokens.getData();
    assertEquals(expectedTokens, actualTokens);
  }

  @Test
  public void testTokeniseLong() {
    String rule = "SCHEMA: 'schema.csv' (REDCAP)\n" +
      "\n" +
      "SERVER: 'local'\n" +
      "\n" +
      "RULES: {\n" +
      "  TRUE { \n" +
      "    Patient<p>:\n" +
      "      * identifier.type = CONCEPT_LITERAL(http://hl7.org/fhir/v2/0203|MC)\n" +
      "      * identifier.type.text = 'Medicare Number'\n" +
      "      * identifier.system = 'http://ns.electronichealth.net.au/id/medicare-number'\n" +
      "      * identifier.value = VALUE(pat_medicare)\n" +
      "  }\n" +
      "\n" +
      "  VALUE(pat_dead) = 1 {\n" +
      "    NOTNULL(pat_dead_date) {\n" +
      "      Patient<p>: * deceasedDateTime = VALUE(pat_dead_date)\n" +
      "    } ELSE {\n" +
      "      Patient<p>: * deceasedBoolean = TRUE\n" +
      "    }\n" +
      "  }\n" +
      "\n" +
      "  VALUE(pat_dead) = 0 {\n" +
      "    Patient<p>: * deceasedBoolean = FALSE\n" +
      "  }\n" +
      "\n" +
      "  NOTNULL(pat_sex) {\n" +
      "    Observation<sex>:\n" +
      "      * code = CONCEPT_LITERAL(http://snomed.info/sct|429019009)\n" +
      "      * valueCodeableConcept = CONCEPT_SELECTED(pat_sex)\n" +
      "      * subject = REF(Patient<p>)\n" +
      "  }\n" +
      "\n" +
      "  VALUE(dx_num) > 0 {\n" +
      "    REPEAT(1..2: x)\n" +
      "    NOTNULL(dx_${x}) {\n" +
      "      VALUE(dx_${x}) = '_NRF_' {\n" +
      "        Condition<c${x}>: \n" +
      "          * code.text = VALUE(dx_text_${x})\n" +
      "          * subject = REF(Patient<p>)\n" +
      "        } ELSE {\n" +
      "        Condition<c${x}>: \n" +
      "          * code = CONCEPT(dx_${x})\n" +
      "          * subject = REF(Patient<p>)\n" +
      "      }\n" +
      "    }\n" +
      "  }\n" +
      "\n" +
      "  REPEAT(1..4: x)\n" +
      "  NOTNULL(phenotype___${x}) {\n" +
      "    Observation<obs${x}>:\n" +
      "      * status = CODE(final)\n" +
      "      * code = CONCEPT(phenotype___${x})\n" +
      "      * interpretation = CONCEPT_LITERAL(" +
      "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation|POS)\n" +
      "  }\n" +
      "}\n" +
      "\n" +
      "MAPPINGS: {\n" +
      "  pat_sex___1 -> http://snomed.info/sct|248153007;\n" +
      "  pat_sex___2 -> http://snomed.info/sct|248152002;\n" +
      "  phenotype___1 -> http://purl.obolibrary.org/obo/hp.owl|HP:0001558;\n" +
      "  phenotype___2 -> http://purl.obolibrary.org/obo/hp.owl|HP:0001270;\n" +
      "  phenotype___3 -> http://purl.obolibrary.org/obo/hp.owl|HP:0031910;\n" +
      "  phenotype___4 -> http://purl.obolibrary.org/obo/hp.owl|HP:0012587;\n" +
      "}";
    printTokens(rule);

    // Encoded
    List<Integer> expectedTokens = Arrays.asList(
      0, 0, 6, 0, 0, 0, 8, 12, 3, 0, 0, 14, 6, 6, 0, 2, 0, 6, 0, 0, 0, 8, 7, 3, 0, 2, 0, 5, 0, 0, 1, 2, 4, 0, 0, 1, 4,
      7, 7, 0, 0, 7, 1, 4, 0, 0, 1, 1, 7, 0, 0, 1, 1, 4, 0, 1, 6, 1, 5, 0, 0, 12, 1, 3, 0, 0, 6, 1, 2, 0, 0, 2, 15, 0,
      0, 0, 15, 1, 2, 0, 0, 1, 27, 7, 0, 0, 28, 2, 7, 0, 1, 6, 1, 5, 0, 0, 12, 1, 3, 0, 0, 5, 1, 3, 0, 0, 6, 1, 2, 0, 0,
      2, 17, 3, 0, 1, 6, 1, 5, 0, 0, 12, 1, 3, 0, 0, 8, 1, 2, 0, 0, 2, 54, 3, 0, 1, 6, 1, 5, 0, 0, 12, 1, 3, 0, 0, 7, 1,
      2, 0, 0, 2, 5, 0, 0, 0, 6, 12, 7, 0, 3, 2, 5, 0, 0, 0, 6, 8, 7, 0, 0, 10, 1, 4, 0, 0, 2, 1, 1, 0, 1, 4, 7, 0, 0,
      0, 8, 13, 7, 0, 1, 6, 7, 7, 0, 0, 7, 1, 4, 0, 0, 1, 1, 7, 0, 0, 1, 1, 4, 0, 0, 3, 1, 5, 0, 0, 19, 1, 2, 0, 0, 2,
      5, 0, 0, 0, 6, 13, 7, 0, 1, 6, 4, 0, 0, 1, 6, 7, 7, 0, 0, 7, 1, 4, 0, 0, 1, 1, 7, 0, 0, 1, 1, 4, 0, 0, 3, 1, 5, 0,
      0, 18, 1, 2, 0, 0, 2, 4, 0, 0, 4, 2, 5, 0, 0, 0, 6, 8, 7, 0, 0, 10, 1, 4, 0, 0, 2, 1, 1, 0, 1, 4, 7, 7, 0, 0, 7,
      1, 4, 0, 0, 1, 1, 7, 0, 0, 1, 1, 4, 0, 0, 3, 1, 5, 0, 0, 18, 1, 2, 0, 0, 2, 5, 0, 0, 3, 2, 7, 0, 0, 0, 8, 7, 7, 0,
      1, 4, 11, 7, 0, 0, 11, 1, 4, 0, 0, 1, 3, 7, 0, 0, 3, 1, 4, 0, 1, 6, 1, 5, 0, 0, 7, 1, 2, 0, 0, 2, 15, 0, 0, 0, 15,
      1, 2, 0, 0, 1, 22, 7, 0, 0, 23, 9, 7, 0, 1, 6, 1, 5, 0, 0, 23, 1, 2, 0, 0, 2, 16, 0, 0, 0, 17, 7, 7, 0, 1, 6, 1,
      5, 0, 0, 10, 1, 2, 0, 0, 2, 3, 0, 0, 0, 4, 7, 7, 0, 0, 7, 1, 4, 0, 0, 1, 1, 7, 0, 0, 1, 1, 4, 0, 3, 2, 5, 0, 0, 0,
      6, 6, 7, 0, 0, 8, 1, 4, 0, 0, 2, 1, 1, 0, 1, 4, 6, 0, 0, 0, 7, 1, 1, 0, 0, 3, 1, 1, 0, 0, 3, 1, 7, 0, 1, 4, 7, 0,
      0, 0, 8, 7, 7, 0, 1, 6, 5, 0, 0, 0, 6, 7, 7, 0, 0, 9, 1, 4, 0, 0, 2, 7, 3, 0, 1, 8, 9, 7, 0, 0, 9, 1, 4, 0, 0, 1,
      5, 7, 0, 0, 5, 1, 4, 0, 1, 10, 1, 5, 0, 0, 6, 1, 3, 0, 0, 6, 1, 2, 0, 0, 2, 5, 0, 0, 0, 6, 12, 7, 0, 1, 10, 1, 5,
      0, 0, 10, 1, 2, 0, 0, 2, 3, 0, 0, 0, 4, 7, 7, 0, 0, 7, 1, 4, 0, 0, 1, 1, 7, 0, 0, 1, 1, 4, 0, 1, 10, 4, 0, 0, 1,
      8, 9, 7, 0, 0, 9, 1, 4, 0, 0, 1, 5, 7, 0, 0, 5, 1, 4, 0, 1, 10, 1, 5, 0, 0, 7, 1, 2, 0, 0, 2, 7, 0, 0, 0, 8, 7, 7,
      0, 1, 10, 1, 5, 0, 0, 10, 1, 2, 0, 0, 2, 3, 0, 0, 0, 4, 7, 7, 0, 0, 7, 1, 4, 0, 0, 1, 1, 7, 0, 0, 1, 1, 4, 0, 5,
      2, 6, 0, 0, 0, 7, 1, 1, 0, 0, 3, 1, 1, 0, 0, 3, 1, 7, 0, 1, 2, 7, 0, 0, 0, 8, 16, 7, 0, 1, 4, 11, 7, 0, 0, 11, 1,
      4, 0, 0, 1, 7, 7, 0, 0, 7, 1, 4, 0, 1, 6, 1, 5, 0, 0, 9, 1, 2, 0, 0, 2, 4, 0, 0, 1, 6, 1, 5, 0, 0, 7, 1, 2, 0, 0,
      2, 7, 0, 0, 0, 8, 16, 7, 0, 1, 6, 1, 5, 0, 0, 17, 1, 2, 0, 0, 2, 15, 0, 0, 0, 15, 1, 2, 0, 0, 1, 66, 7, 0, 0, 67,
      3, 7, 0, 4, 0, 8, 0, 0, 1, 2, 11, 7, 0, 0, 12, 2, 4, 0, 0, 3, 22, 7, 0, 0, 23, 9, 7, 0, 1, 2, 11, 7, 0, 0, 12, 2,
      4, 0, 0, 3, 22, 7, 0, 0, 23, 9, 7, 0, 1, 2, 13, 7, 0, 0, 14, 2, 4, 0, 0, 3, 37, 7, 0, 0, 38, 10, 7, 0, 1, 2, 13,
      7, 0, 0, 14, 2, 4, 0, 0, 3, 37, 7, 0, 0, 38, 10, 7, 0, 1, 2, 13, 7, 0, 0, 14, 2, 4, 0, 0, 3, 37, 7, 0, 0, 38, 10,
      7, 0, 1, 2, 13, 7, 0, 0, 14, 2, 4, 0, 0, 3, 37, 7, 0, 0, 38, 10, 7, 0
    );

    SemanticTokens semanticTokens = SemanticTokeniser.tokenise(rule);
    List<Integer> actualTokens = semanticTokens.getData();
    assertEquals(expectedTokens, actualTokens);
  }

  protected void printTokens(String rule) {
    System.out.println("TOKENS:");
    final Lexer lexer = new RedmatchLexer(CharStreams.fromString(rule));
    for (Token tok : lexer.getAllTokens()) {
      System.out.println(tok);
    }
  }
}
