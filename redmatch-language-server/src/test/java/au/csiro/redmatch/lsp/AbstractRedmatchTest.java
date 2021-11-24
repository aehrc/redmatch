package au.csiro.redmatch.lsp;

import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AbstractRedmatchTest {

  /**
   * Mock client used in tests.
   *
   * @author Alejandro Metke Jimenez
   */
  protected static final class MockClient implements LanguageClient {

    private final List<PublishDiagnosticsParams> diagnostics = new ArrayList<>();
    private boolean publishedDiagnostics = false;

    @Override
    public void telemetryEvent(Object o) {

    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams params) {
      diagnostics.add(params);
      publishedDiagnostics = true;
    }

    @Override
    public void showMessage(MessageParams params) {

    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams params) {
      return null;
    }

    @Override
    public void logMessage(MessageParams params) {

    }

    public List<PublishDiagnosticsParams> getDiagnostics() {
      return diagnostics;
    }

    public boolean isPublishedDiagnostics() {
      return publishedDiagnostics;
    }
  }
}
