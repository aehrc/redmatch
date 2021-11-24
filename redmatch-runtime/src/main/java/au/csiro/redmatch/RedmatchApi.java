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
import au.csiro.redmatch.util.DateUtils;
import au.csiro.redmatch.util.FhirUtils;
import au.csiro.redmatch.util.FileUtils;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Main API for Redmatch.
 *
 * @author Alejandro Metke Jimenez
 *
 */
@Component(value = "api")
public class RedmatchApi {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedmatchApi.class);

  private final FhirContext ctx;

  private final Gson gson;

  private final RedmatchCompiler compiler;

  private final HapiReflectionHelper reflectionHelper;

  private final GraphExporterService graphExporterService;

  @Autowired
  private ObjectProvider<FhirExporter> fhirExporterProvider;

  private static final Range zeroZero = new Range(new Position(0, 0), new Position(0, 0));

  @Autowired
  public RedmatchApi(FhirContext ctx, Gson gson, RedmatchCompiler compiler, HapiReflectionHelper reflectionHelper,
                     GraphExporterService graphExporterService) {
    this.ctx = ctx;
    this.gson = gson;
    this.compiler = compiler;
    this.reflectionHelper = reflectionHelper;
    this.graphExporterService = graphExporterService;
  }

  /**
   * Compiles one or more transformation rules documents.
   *
   * @param baseFolder The base folder where the input files are contained.
   * @param docs The document files.
   * @return The compiled documents.
   */
  public List<Document> compile(File baseFolder, @NotNull File ... docs) {
    log.info("Compiling " + docs.length + " files");
    Instant start = Instant.now();
    List<Document> res = new ArrayList<>();
    for (File doc : docs) {
      log.info("Compiling file " + doc.getName());
      String s = FileUtils.loadTextFile(doc);
      if (baseFolder != null) {
        res.add(compiler.compile(baseFolder, s));
      } else {
        res.add(compiler.compile(s));
      }
    }
    Instant finish = Instant.now();
    long timeElapsed = Duration.between(start, finish).toMillis();
    log.info("Compilation finished in: " + DateUtils.prettyPrintMillis(timeElapsed));
    return res;
  }

  /**
   * Compiles a single transformation rule document. This method does not throw any exceptions but rather uses the
   * document's diagnostics attribute to report any issues, even unexpected runtime exceptions.
   *
   * @param doc The rules document.
   * @return The compiled document.
   */
  public Document compile(@NotNull String doc) {
    try {
      log.info("Compiling rules document");
      Instant start = Instant.now();
      Document res = compiler.compile(doc);
      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      log.info("Compilation finished in: " + DateUtils.prettyPrintMillis(timeElapsed));
      return res;
    } catch (Throwable t) {
      // Catch everything here because we don't want exceptions to propagate beyond this layer
      Document res = new Document();
      res.getDiagnostics().add(new Diagnostic(zeroZero, "Unexpected compilation issue" + t.getLocalizedMessage()));
      return res;
    }
  }

  /**
   * Fetches the data from the source and uses one or more transformation rules documents to generate the FHIR output
   * as ND-JSON files in the output folder.
   *
   * @param serverMap A map of server definitions that are available. The keys are the server definition names.
   * @param baseFolder The base folder where the input files are contained.
   * @param outputFolder The output folder.
   * @param generateGraph Flag to indicate if a graph of the resulting FHIR resource should be generated.
   * @param docFiles The transformation rules documents to transform.
   * @return Map of diagnostic messages. Key is file where error happened.
   */
  public Map<String, List<Diagnostic>> transform(@NotNull Map<String, Server> serverMap, @NotNull File baseFolder,
                                        @NotNull File outputFolder, @NotNull boolean generateGraph,
                                        @NotNull File ... docFiles) {
    Map<String, List<Diagnostic>> errors = new HashMap<>();
    Map<String, DomainResource> res = transform(baseFolder, serverMap, errors, docFiles);
    if (res == null) {
      return errors;
    }

    // Save resource now that we have them all in a map
    try {
      // Create folder if it doesn't exist
      Path tgtDir = Files.createDirectories(outputFolder.toPath());

      // Group resources by type
      final Map<String, List<DomainResource>> grouped = new HashMap<>();
      for (String key : res.keySet()) {
        DomainResource dr = res.get(key);
        String resourceType = dr.getResourceType().toString();
        List<DomainResource> list = grouped.computeIfAbsent(resourceType, k -> new ArrayList<>());
        list.add(dr);
      }

      // Save to folder in ND-JSON
      IParser jsonParser = ctx.newJsonParser();
      for (String key : grouped.keySet()) {
        File f = new File(tgtDir.toFile(), key + ".ndjson");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
          for(DomainResource dr : grouped.get(key)) {
            jsonParser.encodeResourceToWriter(dr, bw);
            bw.newLine();
          }
        }
      }

      // Generate graph
      if (generateGraph) {
        log.info("Generating graph");
        Graph graph = generateGraph(res.values());
        graphExporterService.exportGraph(graph, tgtDir.toFile());
      }
      return errors;
    } catch (IOException e) {
      throw new RuntimeException("There was a problem writing the files to disk: " + e.getLocalizedMessage());
    }
  }

  /**
   * Exports a graph representation of the generated resources.
   *
   * @param resources A collection of FHIR resources.
   * @return A graph representation of the FHIR resources.
   */
  public Graph generateGraph(Collection<DomainResource> resources) {
    log.info("Creating graph representation for visualisation.");

    Graph graph = new Graph();

    // Add vertices
    for (Resource res : resources) {
      graph.addNode(new Node(generateId(res)));
    }

    // Add edges
    for (Resource src : resources) {
      for (FhirUtils.Target tgt : FhirUtils.getReferencedResources(src)) {
        graph.addLink(new Link(generateId(src), tgt.getResourceId(), tgt.getAttributeName()));
      }
    }

    return graph;
  }

  private String generateId(Resource res) {
    return res.fhirType() + "<" + res.getId() + ">";
  }

  private Map<String, DomainResource> transform(File baseFolder, @NotNull Map<String, Server> serverMap,
                                                 @NotNull Map<String, List<Diagnostic>> diagnostics,
                                                 @NotNull File ... docFiles) {
    List<Document> docs = compile(baseFolder, docFiles);
    for (int i = 0; i < docs.size(); i++) {
      Document doc = docs.get(i);
      diagnostics.put(docFiles[i].getName(), doc.getDiagnostics());
      if (doc.getDiagnostics().stream().anyMatch(d -> d.getSeverity().equals(DiagnosticSeverity.Error))) {
        log.debug("Compilation errors were found.");
        return null;
      }
    }

    // Transform documents
    log.info("Transforming " + docFiles.length + " documents");
    Map<String, DomainResource> res = new HashMap<>();
    int index = 0;
    for (Document doc : docs) {
      log.info("Transforming document " + docFiles[index].getName());

      Instant start = Instant.now();
      String serverName = doc.getServer();
      if (serverName == null) {
        List<Diagnostic> ds = diagnostics.computeIfAbsent(docFiles[index].getName(), k -> new ArrayList<>());
        ds.add(new Diagnostic(zeroZero, "No server defined for document #" + index, DiagnosticSeverity.Error, "API"));
        continue;
      }

      Server server = serverMap.get(serverName);
      if (server == null) {
        List<Diagnostic> ds = diagnostics.computeIfAbsent(docFiles[index].getName(), k -> new ArrayList<>());
        ds.add(new Diagnostic(zeroZero, "The server " + serverName
          + " does not exist. Available server definitions are " + serverMap.keySet() + ".", DiagnosticSeverity.Error,
          "API"));
        continue;
      }

      log.info("Getting data from server: " + server.getUrl());

      // Call external data source using client and get rows
      Client client = null;
      switch(server.getType()) {
        case REDCAP:
          client = new RedcapClient(gson);
          break;
        case CSV_OAUTH2:
          throw new UnsupportedOperationException("Support for CSV files over OAuth2 has not been implemented yet.");
      }
      List<Row> rows = client.getData(server.getUrl(), new RedcapCredentials(server.getToken()),
        doc.getReferencedFields(true));
      log.info("Got " + rows.size() + " rows");
      FhirExporter exp;
      if (fhirExporterProvider == null) {
        exp = new FhirExporter(doc, rows, reflectionHelper);
      } else {
        exp = fhirExporterProvider.getObject(doc, rows);
      }

      res.putAll(exp.transform());
      index++;

      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      log.info("Transformation finished in: " + DateUtils.prettyPrintMillis(timeElapsed));
    }
    return res;
  }

}
