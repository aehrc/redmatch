package au.csiro.redmatch;

import au.csiro.redmatch.compiler.Document;
import au.csiro.redmatch.model.Server;
import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
      if (generateGraph || transform) {
        // Run the transformation process
        Map<String, Server> serverMap = loadAllServerMaps(baseFolder);
        printInfo("Checking output folder");
        File outputFolder = null;
        try {
          outputFolder = Files.createDirectories(new File(baseFolder, "output").toPath()).toFile();
        } catch (IOException e) {
          printError("Unable to create output folder: " + e.getLocalizedMessage());
          System.exit(-1);
        }
        Map<String, List<Diagnostic>> diagnostics =  api.transform(serverMap, baseFolder, outputFolder, generateGraph,
          rdmFiles.toArray(new File[0]));
        printDiagnostics(diagnostics);
        if (hasErrors(diagnostics)) {
          printError("TRANSFORMATION FAILURE");
        } else {
          printInfo("TRANSFORMATION SUCCESS");
        }
      } else {
        // Run the compilation process
        File[] files = rdmFiles.toArray(new File[0]);
        List<Document> docs = api.compile(baseFolder, files);
        Map<String, List<Diagnostic>> diagnostics = new HashMap<>();
        int index = 0;
        boolean hasErrors = false;
        for(Document doc : docs) {
          diagnostics.put(files[index].getName(), doc.getDiagnostics());
          if (doc.getDiagnostics().stream().anyMatch(d -> d.getSeverity().equals(DiagnosticSeverity.Error))) {
            hasErrors = true;
          }
          index++;
        }
        printDiagnostics(diagnostics);
        if (hasErrors) {
          printError("BUILD FAILURE");
        } else {
          printInfo("BUILD SUCCESS");
        }
      }
    } catch (ParseException exp) {
      printError(exp.getMessage());
      printUsage(options);
    }

    exit(0);
  }

  private Map<String, Server> loadServerMap(File configFile) {
    log.info("Loading server configuration from " + configFile.getAbsolutePath());
    Map<String, Server> res = new HashMap<>();
    // Read configuration file
    Yaml yaml = new Yaml(new Constructor(Configuration.class));
    try (FileReader fr = new FileReader(configFile)) {
      Configuration conf = yaml.load(fr);
      for (au.csiro.redmatch.Server s : conf.getServers()) {
        res.put(s.getName(), new Server(s.getName(), s.getUrl(), s.getToken()));
      }
    } catch (IOException e) {
      printError("There was a problem reading the configuration file redmatch-config.yaml");
      System.exit(-1);
    }
    return res;
  }

  private Map<String, Server> loadAllServerMaps(File baseFolder) {
    printInfo("Loading server information");
    Map<String, Server> res = new HashMap<>();
    boolean configExists = false;

    Map<String, Server> localMap = new HashMap<>();
    File configFile = new File(baseFolder, "redmatch-config.yaml");
    if (configFile.exists() && configFile.canRead()) {
      localMap = loadServerMap(configFile);
      configExists = true;
    }

    Map<String, Server> userMap = new HashMap<>();
    File userHome = new File(System.getProperty("user.home"));
    File userFolder = new File(userHome, ".redmatch");
    File userConfigFile = new File(userFolder, "redmatch-config.yaml");
    if (userConfigFile.exists() && userConfigFile.canRead()) {
      userMap = loadServerMap(userConfigFile);
      configExists = true;
    }

    if (!configExists) {
      printError("Configuration file redmatch-config.yaml does not exist or could not be read");
      System.exit(-1);
    }

    // Local server definitions have precedence over user definitions
    res.putAll(userMap);
    res.putAll(localMap);

    return res;
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

  private boolean hasErrors(Map<String, List<Diagnostic>> diagnostics) {
    for (String key: diagnostics.keySet()) {
      if (diagnostics.get(key).stream().anyMatch(d -> d.getSeverity().equals(DiagnosticSeverity.Error))) {
        return true;
      }
    }
    return false;
  }

  private void printDiagnostics(Map<String, List<Diagnostic>> diagnosticsMap) {
    for (String key : diagnosticsMap.keySet()) {
      List<Diagnostic> diagnostics = diagnosticsMap.get(key);
      if (!diagnostics.isEmpty()) {
        printInfo("There were problems with file " + key + ":");
        for (Diagnostic diagnostic : diagnostics) {
          printDiagnostic(diagnostic);
        }
      }
    }
  }

}
