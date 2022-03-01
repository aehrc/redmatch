/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import au.csiro.redmatch.compiler.CompilationException;
import au.csiro.redmatch.compiler.Document;
import au.csiro.redmatch.model.Schema;
import au.csiro.redmatch.model.VersionedFhirPackage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Runs diagnostics on Redmatch documents.
 *
 * @author Alejandro Metke Jimenez
 */
public class DiagnosticRunner {

  /** Logger. */
  private static final Log log = LogFactory.getLog(DiagnosticRunner.class);

  /**
   * Used to send diagnostic messages to the client.
   */
  private final RedmatchLanguageServer languageServer;

  private FutureTask<Void> task;

  /**
   * Constructor.
   *
   * @param languageServer Language server reference.
   */
  public DiagnosticRunner(RedmatchLanguageServer languageServer) {
    this.languageServer = languageServer;
  }

  public void compute(DidOpenTextDocumentParams params) {
    String text = params.getTextDocument().getText();
    computeDiagnostics(text, params.getTextDocument());
  }

  public void compute(DidChangeTextDocumentParams params) {
    String text = params.getContentChanges().get(0).getText();
    computeDiagnostics(text,
      languageServer.getTextDocumentService().getOpenedDocument(params.getTextDocument().getUri()));
  }

  public void compute(DidSaveTextDocumentParams params) {
    String text = retrieveFullText(params);
    computeDiagnostics(text,
      languageServer.getTextDocumentService().getOpenedDocument(params.getTextDocument().getUri()));
  }

  public synchronized void computeDiagnostics(String text, TextDocumentItem documentItem) {
    String uri = documentItem.getUri();
    log.info("Computing diagnostics for document " + uri);
    log.debug("Task is done: " + ((task != null) ? task.isDone() : "NA"));

    if (task != null && !task.isDone()) {
      log.info("Found existing task so cancelling it");
      task.cancel(true);
    }

    task = new FutureTask<>(() -> {
      log.debug("Creating new diagnostic runner");
      Document doc;
      try {
        doc = languageServer.getApi().compile(text, uri, null);
      } catch (CompilationException e) {
        // If something goes wrong then don't return any partial results because they could be wrong
        return null;
      }

      List<Diagnostic> diagnostics = doc.getDiagnostics();
      log.info("Compilation produced " + diagnostics.size() + " diagnostic messages");
      if (Thread.interrupted()) {
        log.debug("Interrupting compilation");
        return null;
      }
      languageServer.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
      Schema schema = doc.getSchema();
      if (schema != null) {
        languageServer.getTextDocumentService().setSchema(uri, schema);
      }

      VersionedFhirPackage fhirPackage = doc.getFhirPackage();
      if (fhirPackage != null) {
        languageServer.getTextDocumentService().setFhirPackage(uri, fhirPackage);
      } else {
        languageServer.getTextDocumentService().setFhirPackage(uri, languageServer.getDefaultFhirPackage());
      }

      return null;
    });
    Executors.newCachedThreadPool().submit(task);
  }

  private String retrieveFullText(DidSaveTextDocumentParams params) {
    String text = params.getText();
    if (text == null) {
      text = languageServer.getTextDocumentService().getOpenedDocument(params.getTextDocument().getUri()).getText();
    }
    return text;
  }

  /**
   * Clears the diagnostic messages for a document.
   *
   * @param uri The document's URI.
   */
  public void clear(String uri) {
    languageServer.publishDiagnostics(new PublishDiagnosticsParams(uri, Collections.emptyList()));
  }

}
