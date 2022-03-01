/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import au.csiro.redmatch.util.Progress;
import au.csiro.redmatch.util.ProgressReporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of the {@link ProgressReporter} interface that knows how to report progress using the language server
 * protocol.
 *
 * @author Alejandro Metke Jimenez
 */
public class LspProgressReporter implements ProgressReporter {

  private static final Log log = LogFactory.getLog(LspProgressReporter.class);
  private final RedmatchLanguageServer languageServer;
  private String token;

  public LspProgressReporter(RedmatchLanguageServer languageServer) {
    this.languageServer = languageServer;
  }

  @Override
  public void reportProgress(Progress progress) {
    switch (progress.getStage()) {
      case START:
        if (token != null) {
          notifyEnd(token);
        }
        token = UUID.randomUUID().toString();
        try {
          notifyStart(token, progress.getMessage());
        } catch (ExecutionException | InterruptedException e) {
          log.error(e);
        }
        break;
      case PROGRESS:
        notifyProgress(token, progress.getPercentage());
        break;
      case END:
        notifyEnd(token);
        break;
    }
  }

  private void notifyStart(String token, String message)
    throws ExecutionException, InterruptedException {
    CompletableFuture<Void> completableFuture = languageServer.getClient().createProgress(
      new WorkDoneProgressCreateParams(Either.forLeft(token)));
    if (completableFuture != null) {
      completableFuture.get();
    }
    WorkDoneProgressBegin begin = new WorkDoneProgressBegin();
    begin.setMessage(message);
    begin.setCancellable(true);
    begin.setPercentage(0);
    begin.setTitle("Redmatch");
    ProgressParams progressParams = new ProgressParams(Either.forLeft(token), Either.forLeft(begin));
    languageServer.getClient().notifyProgress(progressParams);
  }

  private void notifyProgress(String token, int percentage) {
    WorkDoneProgressReport report = new WorkDoneProgressReport();
    report.setCancellable(true);
    report.setPercentage(percentage);
    ProgressParams progressParams = new ProgressParams(Either.forLeft(token), Either.forLeft(report));
    languageServer.getClient().notifyProgress(progressParams);
  }

  private void notifyEnd(String token) {
    WorkDoneProgressEnd end = new WorkDoneProgressEnd();
    end.setMessage("Done");
    ProgressParams progressParams = new ProgressParams(Either.forLeft(token), Either.forLeft(end));
    languageServer.getClient().notifyProgress(progressParams);
  }
}
