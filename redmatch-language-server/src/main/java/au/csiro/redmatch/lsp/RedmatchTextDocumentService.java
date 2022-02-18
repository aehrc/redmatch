/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import au.csiro.redmatch.lsp.completion.CompletionProcessor;
import au.csiro.redmatch.model.Schema;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.terminology.TerminologyService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * The Redmatch text document service.
 *
 * @author Alejandro Metke Jimenez
 */
public class RedmatchTextDocumentService implements TextDocumentService {

  private static final Log log = LogFactory.getLog(RedmatchTextDocumentService.class);
  private final RedmatchLanguageServer languageServer;
  private final Map<String, TextDocumentItem> openedDocuments = new ConcurrentHashMap<>();
  private final Map<String, Schema> openedSchemas = new ConcurrentHashMap<>();
  private final Map<String, VersionedFhirPackage> openedFhirPackages = new ConcurrentHashMap<>();
  private final DiagnosticRunner diagnosticRunner;
  private final TerminologyService terminologyService;

  public RedmatchTextDocumentService(RedmatchLanguageServer languageServer, TerminologyService terminologyService) {
    this.languageServer = languageServer;
    this.diagnosticRunner = new DiagnosticRunner(languageServer);
    this.terminologyService = terminologyService;
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    TextDocumentItem textDocument = params.getTextDocument();
    openedDocuments.put(textDocument.getUri(), textDocument);
    diagnosticRunner.compute(params);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    List<TextDocumentContentChangeEvent> contentChanges = params.getContentChanges();
    TextDocumentItem textDocumentItem = openedDocuments.get(params.getTextDocument().getUri());
    if (!contentChanges.isEmpty()) {
      textDocumentItem.setText(contentChanges.get(0).getText());
      diagnosticRunner.compute(params);
    }
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    log.info("didClose: " + params.getTextDocument());
    String uri = params.getTextDocument().getUri();
    openedDocuments.remove(uri);
    openedSchemas.remove(uri);
    diagnosticRunner.clear(uri);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    log.info("didSave: " + params.getTextDocument());
    diagnosticRunner.compute(params);
  }

  @Override
  public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
    log.info("Computing semantic tokens for document " + params.getTextDocument().getUri());
    TextDocumentItem textDocumentItem = openedDocuments.get(params.getTextDocument().getUri());
    final String text = textDocumentItem.getText();

    CompletableFuture<SemanticTokens> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      completableFuture.complete(SemanticTokeniser.tokenise(text));
      return null;
    });

    return completableFuture;
  }

  public TextDocumentItem getOpenedDocument(String uri) {
    return openedDocuments.get(uri);
  }

  @Override
  public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
    return CompletableFutures.computeAsync(cancelToken -> {
      TextDocumentItem document = openedDocuments.get(params.getTextDocument().getUri());
      assert(document != null);
      return QuickFixGenerator.computeCodeActions(params, cancelToken, document);
    });
  }

  @Override
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams completionParams) {
    log.info("Running completions: " + completionParams);
    return CompletableFutures.computeAsync(cancelToken -> {
      String uri = completionParams.getTextDocument().getUri();
      TextDocumentItem textDocumentItem = openedDocuments.get(uri);
      if (textDocumentItem != null) {
        Position position = completionParams.getPosition();
        List<CompletionItem> result = new CompletionProcessor(this, terminologyService)
          .getCompletions(uri, textDocumentItem.getText(), position);
        log.info("Generated " + result.size() + " completion results");
        return Either.forLeft(result);
      }
      return null;
    });
  }

  @Override
  public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
    // Doesn't do anything - implemented only because VSCode seems to call it despite the CompletionItem already having
    // all the information required
    return CompletableFutures.computeAsync(cancelToken -> unresolved);
  }

  public synchronized Schema getSchema(String uri) {
    return openedSchemas.get(uri);
  }

  public synchronized void setSchema(String uri, Schema schema) {
    openedSchemas.put(uri, schema);
  }

  public synchronized VersionedFhirPackage getFhirPackage(String uri) {
    return openedFhirPackages.get(uri);
  }

  public synchronized void setFhirPackage(String uri, VersionedFhirPackage fhirPackage) {
    openedFhirPackages.put(uri, fhirPackage);
  }

}
