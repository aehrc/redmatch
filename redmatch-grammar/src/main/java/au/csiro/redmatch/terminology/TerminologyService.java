/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.terminology;

import au.csiro.ontoserver.api.InternalApi;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.util.DateUtils;
import au.csiro.redmatch.validation.RedmatchGrammarCodeSystemGenerator;
import ca.uhn.fhir.context.FhirContext;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.Parameters;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

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

  private final RedmatchGrammarCodeSystemGenerator generator;

  /**
   * Constructor.
   *
   * @param gson An instance of GSON.
   * @param ctx The FHIR context.
   */
  public TerminologyService(FhirContext ctx, Gson gson) {
    log.info("Initialising terminology service");
    onto = new InternalApi(gson, ctx);
    generator = new RedmatchGrammarCodeSystemGenerator(gson, ctx);
  }

  /**
   * Adds support for a FHIR package to the terminology service. Checks if the corresponding index is installed and if
   * it isn't then attempts to generate the code system and index it.
   *
   * @param fhirPackage The FHIR package to add.
   */
  public void addPackage(VersionedFhirPackage fhirPackage) {
    log.debug("Checking if FHIR package " + fhirPackage + " is installed");
    try {
      Instant start = Instant.now();
      if (!onto.isIndexed(REDMATCH_PREFIX + fhirPackage.getName(), fhirPackage.getVersion())) {
        log.debug("Package is not indexed");
        CodeSystem cs = generator.createCodeSystem(fhirPackage);
        onto.indexFhirCodeSystem(cs);
      } else {
        log.debug("Validation code system for package " + fhirPackage + " is present");
      }
      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      log.info("Finished checking in: " + DateUtils.prettyPrintMillis(timeElapsed));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Parameters validate(VersionedFhirPackage fhirPackage, String path) throws IOException {
    return onto.validateCode(REDMATCH_PREFIX + fhirPackage.getName(), fhirPackage.getVersion(), path, null);
  }

  public Parameters lookup(VersionedFhirPackage fhirPackage, String path) throws IOException {
    return onto.lookup(REDMATCH_PREFIX + fhirPackage.getName(), fhirPackage.getVersion(), path,
      Arrays.asList("min", "max", "type", "targetProfile"));
  }

}
