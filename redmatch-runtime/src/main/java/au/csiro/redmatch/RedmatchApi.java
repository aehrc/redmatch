/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch;

import au.csiro.redmatch.client.Client;
import au.csiro.redmatch.client.RedcapClient;
import au.csiro.redmatch.client.RedcapCredentials;
import au.csiro.redmatch.compiler.Document;
import au.csiro.redmatch.compiler.RedmatchCompiler;
import au.csiro.redmatch.exporter.FhirExporter;
import au.csiro.redmatch.exporter.GraphExporterService;
import au.csiro.redmatch.exporter.HapiReflectionHelper;
import au.csiro.redmatch.model.*;
import au.csiro.redmatch.terminology.TerminologyService;
import au.csiro.redmatch.util.*;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main API for Redmatch.
 *
 * @author Alejandro Metke Jimenez
 *
 */
public class RedmatchApi {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchApi.class);

  private final FhirContext ctx;

  private final Gson gson;

  private final RedmatchCompiler compiler;

  private final HapiReflectionHelper reflectionHelper;

  private final GraphExporterService graphExporterService;

  private final TerminologyService terminologyService;


  public static final Range zeroZero = new Range(new Position(0, 0), new Position(0, 0));

  public RedmatchApi(FhirContext ctx, Gson gson, RedmatchCompiler compiler, HapiReflectionHelper reflectionHelper,
                     GraphExporterService graphExporterService, TerminologyService terminologyService) {
    this.ctx = ctx;
    this.gson = gson;
    this.compiler = compiler;
    this.reflectionHelper = reflectionHelper;
    this.graphExporterService = graphExporterService;
    this.terminologyService = terminologyService;
  }

  /**
   * Compiles a single transformation rule document. This method does not throw any exceptions but rather uses the
   * document's diagnostics attribute to report any issues, even unexpected runtime exceptions.
   *
   * @param doc The rules document.
   * @param name The name of the document.
   * @return The compiled document.
   */
  public synchronized Document compile(@NotNull String doc, @NotNull String name, ProgressReporter progressReporter) {
    try {
      log.info("Compiling rules document");
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportStart("Compiling file " + name));
      }
      Instant start = Instant.now();
      Document res = compiler.compile(doc);
      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      log.info("Compilation finished in: " + DateUtils.prettyPrintMillis(timeElapsed));
      return res;
    } finally {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportEnd());
      }
    }
  }

  /**
   * Compiles one or more transformation rules documents.
   *
   * @param baseFolder The base folder where the input files are contained.
   * @param progressReporter An object used to report progress.
   * @return The compiled documents.
   */
  public List<Document> compile(File baseFolder, ProgressReporter progressReporter) {
    log.info("Compiling rule documents in folder " + baseFolder);

    // Get .rdm files
    List<File> rdmFiles;
    try (Stream<Path> walk = Files.walk(baseFolder.toPath())) {
      rdmFiles = walk
        .filter(p -> !Files.isDirectory(p))
        .map(Path::toFile)
        .filter(f -> f.getName().endsWith(".rdm"))
        .collect(Collectors.toList());
    } catch (IOException e) {
      Document res = new Document();
      res.getDiagnostics().add(new Diagnostic(zeroZero, "Unexpected compilation issue" + e.getLocalizedMessage(),
        DiagnosticSeverity.Error, "API"));
      return List.of(res);
    }

    List<Document> res = new ArrayList<>();
    for (File doc : rdmFiles) {
      log.info("Compiling file " + doc.getName());
      String s = FileUtils.loadTextFile(doc);
      res.add(compile(s, doc.getName(), progressReporter));
    }
    return res;
  }

  /**
   * Runs an operation on a Redmatch rules document.
   *
   * @param operation The operation to run.
   * @param redmatchRulesFile The Redmatch rules document.
   * @param progressReporter An object used to report progress. Can be null.
   * @return List of diagnostic messages.
   */
  public List<Diagnostic> run(@NotNull Operation operation, @NotNull File redmatchRulesFile,
                                    ProgressReporter progressReporter, CancelChecker cancelToken) {
    try {
      File baseFolder = redmatchRulesFile.toPath().getParent().toFile();

      // Compile
      String doc = FileUtils.loadTextFile(redmatchRulesFile);
      String name = redmatchRulesFile.getName();
      Document document = compile(doc, name, progressReporter);
      if (document.getDiagnostics().stream().anyMatch(d -> d.getSeverity().equals(DiagnosticSeverity.Error))) {
        return document.getDiagnostics();
      }

      if (cancelToken != null && cancelToken.isCanceled()) {
        return document.getDiagnostics();
      }

      // Get data from server
      Map<String, DataSource> dataSourceMap = getDataSourceMap(baseFolder);
      String server = document.getServer();
      log.info("Resolving server " + server);
      DataSource dataSource = dataSourceMap.get(server);
      if (dataSource == null) {
        return List.of(new Diagnostic(zeroZero, "Unknown server " + server + ". Available servers are: "
          + dataSourceMap.keySet(), DiagnosticSeverity.Error, "API"));
      }

      log.info("Getting data from server: " + dataSource.getUrl());
      Client client = null;
      switch(dataSource.getType()) {
        case REDCAP:
          client = new RedcapClient(gson);
          break;
        case CSV_OAUTH2:
          throw new UnsupportedOperationException("Support for CSV files over OAuth2 has not been implemented yet.");
      }
      List<Row> rows;
      try {
        if (progressReporter != null) {
          progressReporter.reportProgress(Progress.reportStart("Fetching data"));
        }
        rows = client.getData(dataSource.getUrl(), new RedcapCredentials(dataSource.getToken()),
          document.getReferencedFields(true));
        log.info("Got " + rows.size() + " rows");
      } finally {
        if (progressReporter != null) {
          progressReporter.reportProgress(Progress.reportEnd());
        }
      }

      if (cancelToken != null && cancelToken.isCanceled()) {
        return document.getDiagnostics();
      }

      log.info("Transforming into FHIR resources");
      FhirExporter exp = new FhirExporter(document, rows, reflectionHelper, terminologyService,
        compiler.getDefaultFhirPackage());
      Map<String, DomainResource> resourceMap = exp.transform(progressReporter, null);

      // Group resources by type
      final Map<String, List<DomainResource>> grouped = new HashMap<>();
      for (String key : resourceMap.keySet()) {
        DomainResource dr = resourceMap.get(key);
        String resourceType = dr.getResourceType().toString();
        List<DomainResource> list = grouped.computeIfAbsent(resourceType, k -> new ArrayList<>());
        list.add(dr);
      }

      log.info("Running operation " + operation.toString());
      Path outputFolder = createOutputFolder(baseFolder).toPath();
      switch (operation) {
        case EXPORT:
          save(grouped, outputFolder, progressReporter, cancelToken);
          break;
        case GENERATE_GRAPH:
          generateGraph(resourceMap, outputFolder, progressReporter, cancelToken);
          break;
        case BOTH:
          save(grouped, outputFolder, progressReporter, cancelToken);
          generateGraph(resourceMap, outputFolder, progressReporter, cancelToken);
          break;
      }

      return document.getDiagnostics();
    } catch (Exception e) {
      log.error(e);
      return List.of(new Diagnostic(zeroZero, "Could not complete transformation:" + e.getLocalizedMessage(),
        DiagnosticSeverity.Error, "API"));
    }
  }

  /**
   * Runs an operation on all the Redmatch rule documents found in the base folder.
   *
   * @param operation The operation to run.
   * @param baseFolder The folder that contains the Redmatch rule documents , one or more schemas referenced by the
   *                   rules and a redmatch-config.yaml file with source server details.
   * @param progressReporter An object used to report progress. Can be null.
   * @return Map of diagnostic messages. Key is file where error happened.
   */
  public List<Diagnostic> runAll(@NotNull Operation operation, @NotNull File baseFolder,
                                 ProgressReporter progressReporter, CancelChecker cancelToken) {
    if (!baseFolder.canRead() || !baseFolder.canWrite()) {
      return List.of(new Diagnostic(zeroZero, "Unable to read or write on the base folder.", DiagnosticSeverity.Error,
        "API"));
    }
    // Get .rdm files
    List<File> rdmFiles;
    try (Stream<Path> walk = Files.walk(baseFolder.toPath())) {
      rdmFiles = walk
        .filter(p -> !Files.isDirectory(p))
        .map(Path::toFile)
        .filter(f -> f.getName().endsWith(".rdm"))
        .collect(Collectors.toList());
    } catch (IOException e) {
      return List.of(new Diagnostic(zeroZero, "Unexpected I/O error: " + e.getLocalizedMessage(),
        DiagnosticSeverity.Error, "API"));
    }

    List<Diagnostic> diagnostics = new ArrayList<>();
    for (File rdmFile : rdmFiles) {
      diagnostics.addAll(run(operation, rdmFile, progressReporter, cancelToken));
    }
    return diagnostics;
  }

  /**
   * Exports a graph representation of the generated resources.
   *
   * @param resources A collection of FHIR resources.
   * @param progressReporter An object used to report progress.
   * @param cancelToken A token to check if the operation has been cancelled.
   * @return A graph representation of the FHIR resources.
   */
  public D3Graph generateGraph(Collection<DomainResource> resources, ProgressReporter progressReporter,
                               CancelChecker cancelToken) {
    try {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportStart("Generating graph"));
      }
      log.info("Creating graph representation for visualisation.");

      D3Graph d3Graph = new D3Graph();

      if (cancelToken!= null && cancelToken.isCanceled()) {
        return d3Graph;
      }

      // Add vertices
      for (Resource res : resources) {
        d3Graph.addNode(new D3Node(generateId(res)));
        if (cancelToken != null && cancelToken.isCanceled()) {
          return d3Graph;
        }
      }

      // Add edges
      for (Resource src : resources) {
        for (FhirUtils.Target tgt : FhirUtils.getReferencedResources(src)) {
          d3Graph.addLink(new D3Link(generateId(src), tgt.getResourceId(), tgt.getAttributeName()));
          if (cancelToken != null && cancelToken.isCanceled()) {
            return d3Graph;
          }
        }
      }
      return d3Graph;
    } finally {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportEnd());
      }
    }
  }

  private Map<String, DataSource> getDataSourceMap(File baseFolder) throws IOException, ConfigurationMissingException {
    log.debug("Checking base folder is writable");
    if (!baseFolder.canRead() || !baseFolder.canWrite()) {
      throw new IOException("Unable to read or write on the base folder.");
    }
    log.debug("Getting server information");
    return loadAllServerMaps(baseFolder);
  }

  private File createOutputFolder(File baseFolder) throws IOException {
    log.debug("Creating output folder");
    return Files.createDirectories(new File(baseFolder, "output").toPath()).toFile();
  }

  private Map<String, DataSource> loadServerMap(File configFile) throws IOException {
    log.info("Loading server configuration from " + configFile.getAbsolutePath());
    Map<String, DataSource> res = new HashMap<>();
    // Read configuration file
    Yaml yaml = new Yaml(new Constructor(Configuration.class));
    try (FileReader fr = new FileReader(configFile)) {
      Configuration conf = yaml.load(fr);
      for (au.csiro.redmatch.Server s : conf.getServers()) {
        res.put(s.getName(), new DataSource(s.getName(), s.getUrl(), s.getToken()));
      }
    }
    return res;
  }

  private Map<String, DataSource> loadAllServerMaps(File baseFolder) throws IOException {
    log.info("Loading server information");
    Map<String, DataSource> res = new HashMap<>();
    boolean configExists = false;

    Map<String, DataSource> localMap = new HashMap<>();
    File configFile = new File(baseFolder, "redmatch-config.yaml");
    if (configFile.exists() && configFile.canRead()) {
      localMap = loadServerMap(configFile);
      configExists = true;
    }

    Map<String, DataSource> userMap = new HashMap<>();
    File userHome = new File(System.getProperty("user.home"));
    File userFolder = new File(userHome, ".redmatch");
    File userConfigFile = new File(userFolder, "redmatch-config.yaml");
    if (userConfigFile.exists() && userConfigFile.canRead()) {
      userMap = loadServerMap(userConfigFile);
      configExists = true;
    }

    if (!configExists) {
      throw new ConfigurationMissingException("File redmatch-config.yaml does not exist or could not be read");
    }

    // Local server definitions have precedence over user definitions
    res.putAll(userMap);
    res.putAll(localMap);

    return res;
  }

  private void save(Map<String, List<DomainResource>> grouped, Path tgtDir, ProgressReporter progressReporter,
                    CancelChecker cancelToken)
    throws IOException {
    try {
      log.info("Saving to output folder " + tgtDir);
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportStart("Saving files"));
      }
      IParser jsonParser = ctx.newJsonParser();
      double div = grouped.size() / 100.0;
      int i = 0;
      for (String key : grouped.keySet()) {
        File f = new File(tgtDir.toFile(), key + ".ndjson");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
          for (DomainResource dr : grouped.get(key)) {
            jsonParser.encodeResourceToWriter(dr, bw);
            bw.newLine();
          }
        }
        i++;
        if (progressReporter != null) {
          progressReporter.reportProgress(Progress.reportProgress((int) Math.floor(i / div)));
        }
        if (cancelToken != null && cancelToken.isCanceled()) {
          return;
        }
      }
    } finally {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportEnd());
      }
    }
  }

  private void generateGraph(Map<String, DomainResource> res, Path tgtDir, ProgressReporter progressReporter,
                             CancelChecker cancelToken)
    throws IOException {
    log.info("Generating graph in output folder");
    D3Graph d3Graph = generateGraph(res.values(), progressReporter, cancelToken);
    if (!cancelToken.isCanceled()) {
      graphExporterService.exportGraph(d3Graph, tgtDir.toFile(), progressReporter);
    }
  }

  private String generateId(Resource res) {
    return res.fhirType() + "<" + res.getId() + ">";
  }

}
