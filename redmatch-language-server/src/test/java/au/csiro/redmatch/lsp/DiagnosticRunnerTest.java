/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import org.eclipse.lsp4j.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link DiagnosticRunner} class.
 *
 * @author Alejandro Metke Jimenez
 */
public class DiagnosticRunnerTest extends AbstractRedmatchTest {

  /**
   * Tests the diagnostics for a complex extension, which should be valid.
   */
  @Test
  public void testValidComplexExtension() {
    String rule =
      "SCHEMA: 'schema.json' (REDCAP)\n" +
      "RULES: {\n" +
      "  TRUE {\n" +
      "    ResearchStudy<rstud> :\n" +
      "      * extension.valueQuantity.extension.valueQuantity.extension.valueQuantity.system = " +
      "'http://mysystem.com'\n" +
      "  }\n" +
      "}";

    List<PublishDiagnosticsParams> diagnostics = getDiagnostics("1", rule);
    // We always get "diagnostics" back
    assertEquals(1, diagnostics.size());
    PublishDiagnosticsParams diagnostic = diagnostics.get(0);
    // But there should be no errors
    assertEquals(0, diagnostic.getDiagnostics().size());
  }

  /**
   * Tests the diagnostics for an invalid keyword.
   */
  @Test
  public void testInvalidKeyword() {
    String rule =
      "SCHEMA: 'schema.json' (REDCAP)\n" +
      "RULES: {\n" +
      "  TRUE { \n" +
      "    Patient<p-1>: \n" +
      "      *identifier[0].value = VALUE(record_id) \n" +
      "  }\n" +
      "\n" +
      "  BALUE(pat_sex_xy) = 21 { \n" +
      "    Patient<p>: \n" +
      "      *gender = CODE_LITERAL(male) \n" +
      "  }\n" +
      "}";

    List<PublishDiagnosticsParams> diagnostics = getDiagnostics("1", rule);
    assertEquals(1, diagnostics.size());
    PublishDiagnosticsParams diagnostic = diagnostics.get(0);
    assertEquals("1", diagnostic.getUri());
    assertTrue(diagnostic.getDiagnostics().size() > 0);
  }

  private List<PublishDiagnosticsParams> getDiagnostics(String uri, String text) {
    RedmatchLanguageServer server = new RedmatchLanguageServer();
    server.connect(mockClient);
    DiagnosticRunner diagnosticRunner = new DiagnosticRunner(server);

    TextDocumentItem docItem = new TextDocumentItem(uri, "rdm", 1, text);
    diagnosticRunner.computeDiagnostics(text, docItem);

    while(!mockClient.isPublishedDiagnostics()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return mockClient.getDiagnostics();
  }
}
