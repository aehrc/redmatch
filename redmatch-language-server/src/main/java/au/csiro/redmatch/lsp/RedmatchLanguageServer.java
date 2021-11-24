package au.csiro.redmatch.lsp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The Redmatch language server implementation.
 *
 * @author Alejandro Metke Jimenez
 */
public class RedmatchLanguageServer implements LanguageServer, LanguageClientAware {

  private static final Log log = LogFactory.getLog(RedmatchLanguageServer.class);

  private final RedmatchTextDocumentService textDocumentService;
  private LanguageClient client;

  /**
   * Constructor.
   */
  public RedmatchLanguageServer() {
    textDocumentService = new RedmatchTextDocumentService(this);
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    Integer processId = params.getProcessId();
    if(processId != null) {
      log.info("Parent process ID is " + processId);
    } else {
      log.info("Missing Parent process ID!!");
    }

    ServerCapabilities capabilities = createServerCapabilities();
    InitializeResult result = new InitializeResult(capabilities);
    return CompletableFuture.completedFuture(result);
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    log.info("Client has requested to shut down.");
    return CompletableFuture.completedFuture(new Object());
  }

  @Override
  public void exit() {
    log.info("Client has requested to exit.");
    System.exit(0);
  }

  public RedmatchTextDocumentService getTextDocumentService() {
    return textDocumentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return null;
  }

  @Override
  public void connect(LanguageClient client) {
    this.client = client;
  }

  /**
   * Returns a reference to the client.
   *
   * @return The language client.
   */
  public LanguageClient getClient() {
    return client;
  }

  private ServerCapabilities createServerCapabilities() {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    capabilities.setSemanticTokensProvider(getSemanticTokensProvider());
    return capabilities;
  }

  private SemanticTokensWithRegistrationOptions getSemanticTokensProvider() {
    SemanticTokensLegend legend = new SemanticTokensLegend(Arrays.asList("keyword", "comment", "string", "number",
      "operator", "property", "class", "variable"), Collections.emptyList());

    SemanticTokensWithRegistrationOptions semanticTokensProvider =
      new SemanticTokensWithRegistrationOptions(legend);
    semanticTokensProvider.setFull(true);
    semanticTokensProvider.setRange(false);
    semanticTokensProvider.setDocumentSelector(List.of(new DocumentFilter("redmatch", "file", null)));
    return semanticTokensProvider;
  }

}
