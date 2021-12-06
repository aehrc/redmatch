/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Units tests for {@link QuickFixGenerator}.
 *
 * @author Alejandro Metke Jimenez
 */
public class QuickFixGeneratorTest {

  @Test
  public void testGetPosition() {
    String text = "Line zero\nline one\n\nline three\nline four\nlast line, for sure";
    Position pos = new Position(3, 5);
    assertEquals(25, QuickFixGenerator.getPosition(pos, text));
  }

  @Test
  public void testCalculateSnippet() {
    String text = "SCHEMA: 'schema.csv' (REDCAP)\n" +
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
      "  VALUE(patient_dead) = 1 {\n" +
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
      "}\n" +
      "\n" +
      "MAPPINGS: {\n" +
      "  pat_sex___1 -> http://snomed.info/sct|248153007;\n" +
      "  pat_sex___2 -> http://snomed.info/sct|248152002;\n" +
      "}";

    String expectedMappings = "MAPPINGS: {\n" +
      "  pat_sex___1 -> http://snomed.info/sct|248153007;\n" +
      "  pat_sex___2 -> http://snomed.info/sct|248152002;\n" +
      "}";

    Range range = new Range(new Position(33, 0), new Position(36, 1));
    String mappings = QuickFixGenerator.calculateSnippet(text, range);
    assertEquals(expectedMappings, mappings);
  }

}
