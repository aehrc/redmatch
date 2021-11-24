package au.csiro.redmatch.lsp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Main application class.
 *
 * @author Alejandro Metke
 */
public class Application {

  private static final Log log = LogFactory.getLog(Application.class);

  static RedmatchLanguageServer server;

  public static void main (String[] args) {
    log.info("Launching Redmatch language server.");
    server = new RedmatchLanguageServer();
    Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
    server.connect(launcher.getRemoteProxy());
    launcher.startListening();
  }

}
