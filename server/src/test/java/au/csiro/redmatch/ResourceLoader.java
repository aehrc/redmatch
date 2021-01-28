/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for classes that load resources.
 * 
 * @author Alejandro Metke
 *
 */
public class ResourceLoader {
  
  private static final Log log = LogFactory.getLog(ResourceLoader.class);
  
  protected String loadTestFile(String name) {
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(name).getFile());
    String absolutePath = file.getAbsolutePath();
    log.debug("Loading test file from " + absolutePath);
    
    try (Scanner scanner = new Scanner(new File(absolutePath), "UTF-8")) {
      return scanner.useDelimiter("\\A").next();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  public String loadRulesString(String name) {
    return loadTestFile("rules_" + name + ".rdm");
  }
  
  protected String loadMetadataString(String name) {
    return loadTestFile("metadata_" + name + ".json");
  }
  
  protected String loadReportString(String name) {
    return loadTestFile("report_" + name + ".json");
  }

}
