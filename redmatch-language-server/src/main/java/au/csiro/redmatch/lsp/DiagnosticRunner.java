/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

  public void computeDiagnostics(String text, TextDocumentItem documentItem) {
    String uri = documentItem.getUri();
    log.info("Computing diagnostics for document " + uri);
    CompletableFuture.runAsync(() -> {
      List<Diagnostic> diagnostics = languageServer.getApi().compile(text, uri, null).getDiagnostics();
      log.info("Compilation produced " + diagnostics.size() + " diagnostic messages.");
      languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
    });
  }

  private String retrieveFullText(DidSaveTextDocumentParams params) {
    String camelText = params.getText();
    if (camelText == null) {
      camelText = languageServer.getTextDocumentService().getOpenedDocument(params.getTextDocument().getUri()).getText();
    }
    return camelText;
  }

  /**
   * Clears the diagnostic messages for a document.
   *
   * @param uri The document's URI.
   */
  public void clear(String uri) {
    languageServer.getClient().publishDiagnostics(new PublishDiagnosticsParams(uri, Collections.emptyList()));
  }
}
