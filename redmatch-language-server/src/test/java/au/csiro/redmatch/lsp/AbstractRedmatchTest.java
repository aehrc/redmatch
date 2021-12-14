/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Base class for tests.
 *
 * @author Alejandro Metke Jimenez
 */
public class AbstractRedmatchTest {

  protected MockClient mockClient = new MockClient();

  /**
   * Mock client used in tests.
   *
   * @author Alejandro Metke Jimenez
   */
  public static final class MockClient implements LanguageClient {

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

    @Override
    public CompletableFuture<Void> createProgress(WorkDoneProgressCreateParams params) {
      return null;
    }

    @Override
    public void notifyProgress(ProgressParams params) {

    }
  }
}
