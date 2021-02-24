/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch;

import java.util.List;
import java.util.UUID;

import au.csiro.redmatch.model.RedmatchProject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import au.csiro.redmatch.importer.RedcapImporter;
import au.csiro.redmatch.model.Annotation;
import au.csiro.redmatch.model.Row;

/**
 * Base class for Redmatch tests that need to load the test resources.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@ActiveProfiles("test")
@Component
public abstract class AbstractRedmatchTest extends ResourceLoader {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(AbstractRedmatchTest.class);
  
  @Autowired
  protected RedcapImporter redcapImporter;
  
  protected RedmatchProject loadProject(String name, String rules) {
    RedmatchProject project = new RedmatchProject(UUID.randomUUID().toString(),
            "http://myredcap.org",
            "xxx",
            name);
    String json = loadMetadataString(name);
    redcapImporter.addMetadata(json, project);
    project.setRulesDocument(rules);
    return project;
  }

  protected RedmatchProject loadProject(String name) {
    return loadProject(name, loadTestFile("rules_" + name + ".rdm"));
  }
  
  protected List<Row> loadData(String name) {
    String json = loadReportString(name);
    return redcapImporter.parseData(json);
  }
  
  protected void printErrors(List<Annotation> errors) {
    for (Annotation error : errors) {
      log.info(error);
    }
  }
}
