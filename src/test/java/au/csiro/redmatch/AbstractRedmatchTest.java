/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import au.csiro.redmatch.importer.RedcapImporter;
import au.csiro.redmatch.model.Annotation;
import au.csiro.redmatch.model.Metadata;
import au.csiro.redmatch.model.Row;

/**
 * Base class for Redmatch tests that need to load the test resources.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@ActiveProfiles("test")
@Component
public abstract class AbstractRedmatchTest {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(AbstractRedmatchTest.class);
  
  protected static final String VER_STATUS_URL = 
      "http://terminology.hl7.org/CodeSystem/condition-ver-status";
  
  @Autowired
  protected RedcapImporter redcapImporter;
  
  protected Metadata loadMetadata() {
    String json = loadTestFile("src/test/resources/metadata.json");
    return redcapImporter.parseMetadata(json);
  }
  
  protected List<Row> loadData() {
    String json = loadTestFile("src/test/resources/report.json");
    return redcapImporter.parseData(json);
  }
  
  protected String loadRules() {
    return loadTestFile("src/test/resources/rules_tutorial.fcp");
  }
  
  private String loadTestFile(String path) {
    try (Scanner scanner = new Scanner(new File(path), "UTF-8")) {
      return scanner.useDelimiter("\\A").next();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  protected CodeableConcept getCodeableConcept(String system, String code, String display) {
    CodeableConcept res = new CodeableConcept();
    res.addCoding().setSystem(system).setCode(code).setDisplay(display);
    return res;
  }
  
  /**
   * Assumes codeable concepts have a single coding. Does not consider display.
   * 
   * @param c1 First codeable concept.
   * @param c2 Second codeable concept.
   * @return True if equivalent, false otherwise.
   */
  protected boolean equal(CodeableConcept c1, CodeableConcept c2) {
    if (c1.getCodingFirstRep().getSystem().equals(c2.getCodingFirstRep().getSystem()) 
        && c1.getCodingFirstRep().getCode().equals(c2.getCodingFirstRep().getCode())) {
      return true;
    }
    
    return false;
  }
  
  protected void printErrors(List<Annotation> errors) {
    for (Annotation error : errors) {
      log.info(error);
    }
  }
}
