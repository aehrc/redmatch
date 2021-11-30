package au.csiro.redmatch.lsp;

import au.csiro.redmatch.RedmatchApi;
import au.csiro.redmatch.compiler.RedmatchCompiler;
import au.csiro.redmatch.exporter.GraphExporterService;
import au.csiro.redmatch.exporter.HapiReflectionHelper;
import au.csiro.redmatch.validation.RedmatchGrammarValidator;
import ca.uhn.fhir.context.FhirContext;
import com.google.gson.Gson;
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

  private final RedmatchApi api;

  /**
   * Constructor.
   *
   * @param languageServer Language server reference.
   */
  public DiagnosticRunner(RedmatchLanguageServer languageServer) {
    this.languageServer = languageServer;
    FhirContext ctx = FhirContext.forR4();
    Gson gson = new Gson();
    RedmatchGrammarValidator validator = new RedmatchGrammarValidator(gson, ctx);
    RedmatchCompiler compiler = new RedmatchCompiler(validator, gson);
    HapiReflectionHelper reflectionHelper = new HapiReflectionHelper(ctx);
    GraphExporterService graphExporterService = new GraphExporterService();
    api = new RedmatchApi(ctx, gson, compiler, reflectionHelper, graphExporterService);
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
      List<Diagnostic> diagnostics = api.compile(text).getDiagnostics();
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
