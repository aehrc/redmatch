package au.csiro.redmatch;

import au.csiro.redmatch.compiler.Document;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.System.exit;

/**
 * Main implementation of the command line interface.
 *
 * @author Alejandro Metke Jimenez
 */
public class CommandLineInterface {

  /** Logger. */
  private static final Log log = LogFactory.getLog(CommandLineInterface.class);

  public void run(String[] args, RedmatchApi api) {
    Options options = new Options();
    options.addOption("c", false, "Compile the Redmatch rules.");
    options.addOption("t", false, "Transforms the source data into FHIR using the Redmatch rules.");
    options.addOption("g", false, "Shows a graph of the resulting FHIR resources.");
    CommandLineParser parser = new DefaultParser();

    try {
      // Parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (line.getArgList().isEmpty()) {
        printError("No argument was supplied. Please indicate where to find the Redmatch transformation files.");
        printUsage(options);
        exit(0);
      }

      // Get base folder where Redmatch will run
      File baseFolder = new File(line.getArgList().get(0));
      if (!baseFolder.exists()) {
        printError("Folder " + baseFolder.getAbsolutePath() + " does not exist");
        exit(0);
      }

      if (!baseFolder.canRead() || !baseFolder.canWrite()) {
        printError("Cannot read or write on folder " + baseFolder.getAbsolutePath());
        exit(0);
      }

      // Get .rdm files
      List<File> rdmFiles = null;
      try (Stream<Path> walk = Files.walk(baseFolder.toPath())) {
        rdmFiles = walk
          .filter(p -> !Files.isDirectory(p))
          .map(Path::toFile)
          .filter(f -> f.getName().endsWith(".rdm"))
          .collect(Collectors.toList());
      } catch (IOException e) {
        printError("There was an I/O issue: " + e.getLocalizedMessage());
        System.exit(-1);
      }

      if (rdmFiles.isEmpty()) {
        printError("There are no Redmatch files to compile!");
        System.exit(0);
      }

      boolean generateGraph = line.hasOption("g");
      boolean transform = line.hasOption("t");

      printInfo("Initialising compiler");
      List<Diagnostic> diagnostics;
      if (generateGraph && transform) {
        diagnostics = api.runAll(Operation.BOTH, baseFolder, null);
      } else if (transform) {
        diagnostics = api.runAll(Operation.EXPORT, baseFolder, null);
      } else if (generateGraph) {
        diagnostics = api.runAll(Operation.GENERATE_GRAPH, baseFolder, null);
      } else {
        // Run the compilation process
        List<Document> docs = api.compile(baseFolder, null);
        diagnostics = new ArrayList<>();
        for(Document doc : docs) {
          diagnostics.addAll(doc.getDiagnostics());
        }
      }
      printDiagnostics(diagnostics);
      if (hasErrors(diagnostics)) {
        printError("TRANSFORMATION FAILURE");
      } else {
        printInfo("TRANSFORMATION SUCCESS");
      }
    } catch (ParseException exp) {
      printError(exp.getMessage());
      printUsage(options);
    }
    exit(0);
  }

  private void printDiagnostic(Diagnostic diagnostic) {
    if (diagnostic.getSeverity().equals(DiagnosticSeverity.Warning)) {
      printWarning(diagnostic);
    } else {
      printError(diagnostic);
    }
  }

  private void printError(Diagnostic error) {
    Range r = error.getRange();
    Position start = r.getStart();
    log.error(error.getMessage() + " [" + start.getLine() + "," + start.getCharacter() + "]");
  }

  private void printWarning(Diagnostic warning) {
    Range r = warning.getRange();
    Position start = r.getStart();
    log.warn(warning.getMessage() + " [" + start.getLine() + "," + start.getCharacter() + "]");
  }

  private void printInfo(String msg) {
    log.info(msg);
  }

  private void printError(String msg) {
    log.error(msg);
  }

  private static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    final PrintWriter writer = new PrintWriter(System.out);
    formatter.printUsage(writer, 80, "Redmatch", options);
    writer.flush();
  }

  private boolean hasErrors(List<Diagnostic> diagnostics) {
    return diagnostics.stream().anyMatch(d -> d.getSeverity().equals(DiagnosticSeverity.Error));
  }

  private void printDiagnostics(List<Diagnostic> diagnostics) {
    if (!diagnostics.isEmpty()) {
      printInfo("There were problems:");
      for (Diagnostic diagnostic : diagnostics) {
        printDiagnostic(diagnostic);
      }
    }
  }

}
