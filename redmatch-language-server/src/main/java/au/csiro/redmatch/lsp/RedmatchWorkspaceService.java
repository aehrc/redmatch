/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.lsp;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Redmatch workspace service.
 *
 * @author Alejandro Metke Jimenez
 */
public class RedmatchWorkspaceService implements WorkspaceService {

  private static final Log log = LogFactory.getLog(RedmatchWorkspaceService.class);

  private final RedmatchLanguageServer languageServer;

  private final Map<String, AtomicBoolean> inProgressMap = new HashMap<>();

  public RedmatchWorkspaceService(RedmatchLanguageServer languageServer) {
    this.languageServer = languageServer;
  }

  @Override
  public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
    log.info("Params: " + params);
    String command = params.getCommand();
    List<Object> args = params.getArguments();
    log.info("Processing command: " + command + " with args " + args + " and work done token "
      + params.getWorkDoneToken());

    return CompletableFutures.computeAsync(cancelToken -> {
      if (command.startsWith("au.csiro.redmatch.transform.") || command.startsWith("au.csiro.redmatch.graph.")) {
        JsonObject arg = (JsonObject) args.get(0);
        String scheme = arg.get("scheme").getAsString();

        if (!scheme.equals("file")) {
          languageServer.getClient().showMessage(new MessageParams(MessageType.Warning,
            "This operation can only be run on a file (was run on a " + scheme + ")"));
          return null;
        }
        String path = arg.get("path").getAsString();
        Path filePath = Paths.get(path);
        File parentFolder = filePath.getParent().toFile();

        AtomicBoolean flag = inProgressMap.computeIfAbsent(parentFolder.toString(), k -> new AtomicBoolean(false));
        if (flag.compareAndSet(false, true)) {
          switch (command) {
            case "au.csiro.redmatch.transform.all":
              exportAll(parentFolder, flag, cancelToken);
              break;
            case "au.csiro.redmatch.transform.this":
              exportThis(filePath, flag, cancelToken);
              break;
            default:
              log.error("Unexpected command " + command);
              flag.compareAndSet(true, false);
              break;
          }
        } else {
          languageServer.getClient().showMessage(new MessageParams(MessageType.Warning,
            "An operation on this folder is in progress."));
          return null;
        }
      } else {
        log.info("Unexpected command: " + command);
      }
      return null;
    });
  }

  private void exportThis(Path filePath, AtomicBoolean flag, CancelChecker cancelToken) {
    try {
      List<Diagnostic> diagnostics = languageServer.getApi().export(filePath.toFile(),
        new LspProgressReporter(languageServer), cancelToken);
      for (Diagnostic diagnostic : diagnostics) {
        MessageType messageType = MessageType.Info;
        switch (diagnostic.getSeverity()) {
          case Error:
            messageType = MessageType.Error;
            break;
          case Warning:
            messageType = MessageType.Warning;
            break;
        }
        languageServer.getClient().showMessage(new MessageParams(messageType, diagnostic.getMessage()));
      }
      if (diagnostics.isEmpty()) {
        languageServer.getClient().showMessage(new MessageParams(MessageType.Info, "Done"));
      }
    } finally {
      flag.compareAndSet(true, false);
    }
  }

  private void exportAll(File parentFolder, AtomicBoolean flag, CancelChecker cancelToken) {
    try {
      List<Diagnostic> diagnostics = languageServer.getApi().exportAll(parentFolder,
        new LspProgressReporter(languageServer), cancelToken);
      for (Diagnostic diagnostic : diagnostics) {
        MessageType messageType = MessageType.Info;
        switch (diagnostic.getSeverity()) {
          case Error:
            messageType = MessageType.Error;
            break;
          case Warning:
            messageType = MessageType.Warning;
            break;
        }
        languageServer.getClient().showMessage(new MessageParams(messageType, diagnostic.getMessage()));
      }
      if (diagnostics.isEmpty()) {
        languageServer.getClient().showMessage(new MessageParams(MessageType.Info, "Done"));
      }
    } finally {
      flag.compareAndSet(true, false);
    }
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {

  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {

  }
}
