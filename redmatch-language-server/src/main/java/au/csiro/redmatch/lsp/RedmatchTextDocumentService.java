package au.csiro.redmatch.lsp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class RedmatchTextDocumentService implements TextDocumentService {

  private static final Log log = LogFactory.getLog(RedmatchTextDocumentService.class);
  private final RedmatchLanguageServer languageServer;
  private final Map<String, TextDocumentItem> openedDocuments = new HashMap<>();

  public RedmatchTextDocumentService(RedmatchLanguageServer languageServer) {
    this.languageServer = languageServer;
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    TextDocumentItem textDocument = params.getTextDocument();
    log.info("didOpen: " + textDocument);
    openedDocuments.put(textDocument.getUri(), textDocument);
    new DiagnosticRunner(languageServer).compute(params);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    log.info("didChange: " + params.getTextDocument());
    List<TextDocumentContentChangeEvent> contentChanges = params.getContentChanges();
    TextDocumentItem textDocumentItem = openedDocuments.get(params.getTextDocument().getUri());
    if (!contentChanges.isEmpty()) {
      textDocumentItem.setText(contentChanges.get(0).getText());
      new DiagnosticRunner(languageServer).compute(params);
    }
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    log.info("didClose: " + params.getTextDocument());
    String uri = params.getTextDocument().getUri();
    openedDocuments.remove(uri);
    new DiagnosticRunner(languageServer).clear(uri);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    log.info("didSave: " + params.getTextDocument());
    new DiagnosticRunner(languageServer).compute(params);
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

}