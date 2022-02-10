/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import au.csiro.redmatch.RedmatchApi;
import au.csiro.redmatch.compiler.RedmatchCompiler;
import au.csiro.redmatch.exporter.GraphExporterService;
import au.csiro.redmatch.exporter.HapiReflectionHelper;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.validation.RedmatchGrammarValidator;
import ca.uhn.fhir.context.FhirContext;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * The Redmatch language server implementation.
 *
 * @author Alejandro Metke Jimenez
 */
public class RedmatchLanguageServer implements LanguageServer, LanguageClientAware {

  private static final Log log = LogFactory.getLog(RedmatchLanguageServer.class);

  private final RedmatchTextDocumentService textDocumentService;
  private final RedmatchWorkspaceService workspaceService;
  private LanguageClient client;
  private final RedmatchApi api;

  /**
   * Constructor.
   */
  public RedmatchLanguageServer() {
    FhirContext ctx = FhirContext.forR4();
    Gson gson = new Gson();
    // TODO: would be good to allow users to set the default FHIR package through configuration options
    RedmatchCompiler compiler = new RedmatchCompiler(ctx, gson, new VersionedFhirPackage("hl7.fhir.r4.core", "4.0.1"));
    HapiReflectionHelper reflectionHelper = new HapiReflectionHelper(ctx);
    reflectionHelper.init();
    GraphExporterService graphExporterService = new GraphExporterService();
    api = new RedmatchApi(ctx, gson, compiler, reflectionHelper, graphExporterService);
    textDocumentService = new RedmatchTextDocumentService(this);
    workspaceService = new RedmatchWorkspaceService(this);
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
    return workspaceService;
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

  /**
   * Publishes diagnostics to the client.
   *
   * @param params The diagnostics.
   */
  public synchronized void publishDiagnostics(PublishDiagnosticsParams params) {
    client.publishDiagnostics(params);
  }

  public RedmatchApi getApi() {
    return api;
  }

  private ServerCapabilities createServerCapabilities() {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    capabilities.setSemanticTokensProvider(getSemanticTokensProvider());
    capabilities.setCodeActionProvider(true);
    ExecuteCommandOptions executeCommandOptions = new ExecuteCommandOptions(
      List.of(
        "au.csiro.redmatch.transform.this",
        "au.csiro.redmatch.transform.all",
        "au.csiro.redmatch.graph.this",
        "au.csiro.redmatch.graph.all"
      )
    );
    executeCommandOptions.setWorkDoneProgress(true);
    log.info("Setting execute command capabilities: " + executeCommandOptions);
    capabilities.setExecuteCommandProvider(executeCommandOptions);
    capabilities.setCompletionProvider(new CompletionOptions(Boolean.TRUE, List.of( "(")));
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
