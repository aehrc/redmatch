/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.terminology;

import au.csiro.ontoserver.api.InternalApi;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.util.DateUtils;
import au.csiro.redmatch.util.Progress;
import au.csiro.redmatch.util.ProgressReporter;
import au.csiro.redmatch.validation.RedmatchGrammarCodeSystemGenerator;
import ca.uhn.fhir.context.FhirContext;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.ValueSet;
import org.javatuples.Triplet;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * A service that provides terminology functionality.
 *
 * @author Alejandro Metke-Jimenez
 *
 */
public class TerminologyService {

  private static final Log log = LogFactory.getLog(TerminologyService.class);

  private static final String REDMATCH_PREFIX = "http://redmatch.";

  private final InternalApi onto;

  /**
   * Used to make sure the system does not attempt to index validation code system simultaneously.
   */
  private final BlockingQueue<Triplet<VersionedFhirPackage, CompletableFuture<Void>, ProgressReporter>> blockingQueue =
    new LinkedBlockingDeque<>(10);

  private final ExecutorService executor = Executors.newFixedThreadPool(1);

  /**
   * Constructor.
   *
   * @param gson An instance of GSON.
   * @param ctx The FHIR context.
   */
  public TerminologyService(FhirContext ctx, Gson gson) {
    log.info("Initialising terminology service");
    onto = new InternalApi(gson, ctx);

    Runnable indexingTask = () -> {
      try {
        while (true) {
          Triplet<VersionedFhirPackage, CompletableFuture<Void>, ProgressReporter> item = blockingQueue.take();
          VersionedFhirPackage fhirPackage = item.getValue0();
          ProgressReporter progressReporter = item.getValue2();
          log.debug("Checking if FHIR package " + fhirPackage + " is installed");
          try {
            Instant start = Instant.now();
            log.debug("Package is not indexed");
            RedmatchGrammarCodeSystemGenerator generator = new RedmatchGrammarCodeSystemGenerator(gson, ctx);
            CodeSystem cs = generator.createCodeSystem(fhirPackage, progressReporter);

            if (progressReporter != null) {
              progressReporter.reportProgress(Progress.reportStart("Indexing code system for FHIR package "
                + fhirPackage));
            }
            Path targetFolder = onto.indexFhirCodeSystem(cs);
            if (progressReporter != null) {
              progressReporter.reportProgress(Progress.reportEnd());
            }

            Path targetFile = targetFolder.resolve(fhirPackage.getName() + ".json");
            try (FileWriter fw = new FileWriter(targetFile.toFile())) {
              if (progressReporter != null) {
                progressReporter.reportProgress(Progress.reportStart("Saving generated code system for FHIR package "
                  + fhirPackage));
              }
              ctx.newJsonParser().setPrettyPrint(true).encodeResourceToWriter(cs, fw);
              if (progressReporter != null) {
                progressReporter.reportProgress(Progress.reportEnd());
              }
            }
            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            log.info("Finished checking in: " + DateUtils.prettyPrintMillis(timeElapsed));
            item.getValue1().complete(null);
          } catch (IOException e) {
            log.error("There was an IO error processing FHIR package " + item.getValue0(), e);
            Thread.currentThread().interrupt();
          }
        }
      } catch (InterruptedException e) {
        log.error("Thread was interrupted.", e);
      }
    };
    executor.execute(indexingTask);
  }

  /**
   * This method should be called when the application is about to be shut down.
   */
  public void shutdown() {
    executor.shutdown();
  }

  /**
   * Adds support for a FHIR package to the terminology service. Checks if the corresponding index is installed and if
   * it isn't then attempts to generate the code system and index it.
   *
   * @param fhirPackage  The FHIR package to add.
   * @return A {@link CompletableFuture} that represents this computation.
   */
  public CompletableFuture<Void> addPackage(VersionedFhirPackage fhirPackage) {
    return addPackage(fhirPackage, null);
  }

  /**
   * Adds support for a FHIR package to the terminology service. Checks if the corresponding index is installed and if
   * it isn't then attempts to generate the code system and index it.
   *
   * @param fhirPackage The FHIR package to add.
   * @param progressReporter An object to report progress. Can be null.
   * @return A {@link CompletableFuture} that represents this computation.
   */
  public CompletableFuture<Void> addPackage(VersionedFhirPackage fhirPackage, ProgressReporter progressReporter) {

    CompletableFuture<Void> future = new CompletableFuture<>();

    if (onto.isIndexed(REDMATCH_PREFIX + fhirPackage.getName(), fhirPackage.getVersion())) {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportStart("Adding package " + fhirPackage));
      }
      future.complete(null);
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportEnd());
      }
    } else {
      blockingQueue.add(new Triplet<>(fhirPackage, future, progressReporter));
    }
    return future;
  }

  public Parameters validate(VersionedFhirPackage fhirPackage, String path) throws IOException {
    return onto.validateCode(REDMATCH_PREFIX + fhirPackage.getName(), fhirPackage.getVersion(), path, null);
  }

  public Parameters lookup(VersionedFhirPackage fhirPackage, String path) throws IOException {
    return onto.lookup(REDMATCH_PREFIX + fhirPackage.getName(), fhirPackage.getVersion(), path,
      Arrays.asList("min", "max", "type", "targetProfile"));
  }

  public ValueSet expand(VersionedFhirPackage fhirPackage, String query, boolean isResource, String parentResource)
    throws IOException {
    ValueSet.ConceptSetFilterComponent filter;

    if (isResource) {
      filter = new ValueSet.ConceptSetFilterComponent()
        .setProperty("parentResource")
        .setOp(ValueSet.FilterOperator.EQUAL)
        .setValue("Object");
    } else {
      filter = new ValueSet.ConceptSetFilterComponent()
        .setProperty("parentResource")
        .setOp(ValueSet.FilterOperator.EQUAL)
        .setValue(parentResource);
      query = parentResource + "." + query;
    }

    log.info("Expanding FHIR package " + fhirPackage + " with query " + query);
    return onto.expand(REDMATCH_PREFIX + fhirPackage.getName(), fhirPackage.getVersion(), query, filter);
  }

}
