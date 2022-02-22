/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp.completion;

import au.csiro.redmatch.importer.RedcapJsonImporter;
import au.csiro.redmatch.lsp.AbstractRedmatchTest;
import au.csiro.redmatch.lsp.RedmatchLanguageServer;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.terminology.TerminologyService;
import au.csiro.redmatch.util.FileUtils;
import ca.uhn.fhir.context.FhirContext;
import com.google.gson.Gson;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CompletionProcessor}.
 *
 * @author Alejandro Metke Jimenez
 */
public class CompletionProcessorTest extends AbstractRedmatchTest {

  private static final Gson gson = new Gson();
  private static final TerminologyService terminologyService = new TerminologyService(FhirContext.forR4(), gson);

  @Test
  public void testCompletionsRedcapField() {
    RedmatchLanguageServer server = new RedmatchLanguageServer();
    server.connect(mockClient);
    String schemaString = FileUtils.loadTextFileFromClassPath("simple_schema.json");
    RedcapJsonImporter redcapJsonImporter = new RedcapJsonImporter(gson);
    server.getTextDocumentService().setSchema("1", redcapJsonImporter.loadSchema(schemaString));

    CompletionProcessor completionProcessor = new CompletionProcessor(server.getTextDocumentService(),
      terminologyService);
    String uri = "1";
    String documentString = "SCHEMA: 'simple_schema.json' (REDCAP) RULES: { VALUE(";
    Position position = new Position(0, 52);
    List<CompletionItem> completions = completionProcessor.getCompletions(uri, documentString, position);
    assertEquals(5, completions.size());

    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_dob")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_sex")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_sex___1")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_sex___2")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("record_id")));

    documentString = "SCHEMA: 'simple_schema.json' (REDCAP) RULES: { VALUE(p";
    position = new Position(0, 53);
    completions = completionProcessor.getCompletions(uri, documentString, position);
    assertEquals(4, completions.size());

    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_dob")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_sex")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_sex___1")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_sex___2")));

    documentString = "SCHEMA: 'simple_schema.json' (REDCAP) RULES: { VALUE(pat_s";
    position = new Position(0, 57);
    completions = completionProcessor.getCompletions(uri, documentString, position);
    assertEquals(3, completions.size());

    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_sex")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_sex___1")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("pat_sex___2")));
  }

  @Test
  public void testCompletionsFhirResource() {
    RedmatchLanguageServer server = new RedmatchLanguageServer();
    server.connect(mockClient);
    String schemaString = FileUtils.loadTextFileFromClassPath("simple_schema.json");
    RedcapJsonImporter redcapJsonImporter = new RedcapJsonImporter(gson);
    server.getTextDocumentService().setSchema("1", redcapJsonImporter.loadSchema(schemaString));
    server.getTextDocumentService().setFhirPackage("1", new VersionedFhirPackage("hl7.fhir.r4.core", "4.0.1"));

    CompletionProcessor completionProcessor = new CompletionProcessor(server.getTextDocumentService(),
      terminologyService);
    String uri = "1";
    String documentString = "SCHEMA: 'simple_schema.json' (REDCAP) RULES: { TRUE { ";
    Position position = new Position(0, 52);
    List<CompletionItem> completions = completionProcessor.getCompletions(uri, documentString, position);
    for (CompletionItem completionItem : completions) {
      System.out.println(completionItem.getLabel());
    }
    assertEquals(189, completions.size());

    documentString = "SCHEMA: 'simple_schema.json' (REDCAP) RULES: { TRUE { Obs ";
    position = new Position(0, 56);
    completions = completionProcessor.getCompletions(uri, documentString, position);
    assertEquals(3, completions.size());

    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("Observation")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("ObservationDefinition")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("observation-genetics")));
  }

  @Test
  public void testCompletionsFhirAttribute() {
    RedmatchLanguageServer server = new RedmatchLanguageServer();
    server.connect(mockClient);
    String schemaString = FileUtils.loadTextFileFromClassPath("simple_schema.json");
    RedcapJsonImporter redcapJsonImporter = new RedcapJsonImporter(gson);
    server.getTextDocumentService().setSchema("1", redcapJsonImporter.loadSchema(schemaString));
    server.getTextDocumentService().setFhirPackage("1", new VersionedFhirPackage("hl7.fhir.r4.core", "4.0.1"));

    CompletionProcessor completionProcessor = new CompletionProcessor(server.getTextDocumentService(),
      terminologyService);
    String uri = "1";
    String documentString = "SCHEMA: 'simple_schema.json' (REDCAP) RULES: { TRUE { Observation<sex>: * ";

    Position position = new Position(0, 72);
    List<CompletionItem> completions = completionProcessor.getCompletions(uri, documentString, position);
    assertEquals(497, completions.size());

    documentString = "SCHEMA: 'simple_schema.json' (REDCAP) RULES: { TRUE { Observation<sex>: * code.co ";
    position = new Position(0, 80);
    completions = completionProcessor.getCompletions(uri, documentString, position);
    assertEquals(8, completions.size());

    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("code.coding")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("code.coding.id")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("code.coding.code")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("code.coding.system")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("code.coding.display")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("code.coding.version")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("code.coding.extension")));
    assertTrue(completions.stream().anyMatch(i -> i.getLabel().equals("code.coding.userSelected")));
  }

}
