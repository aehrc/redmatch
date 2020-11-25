/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
 */
package au.csiro.redmatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Base class for classes that load resources.
 * 
 * @author Alejandro Metke
 *
 */
public class ResourceLoader {

  protected String loadTestFile(String path) {
    try (Scanner scanner = new Scanner(new File(path), "UTF-8")) {
      return scanner.useDelimiter("\\A").next();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  
  public String loadRulesString(String name) {
    return loadTestFile("src/test/resources/rules_" + name + ".rdm");
  }
  
  protected String loadMetadataString(String name) {
    return loadTestFile("src/test/resources/metadata_" + name + ".json");
  }
  
  protected String loadReportString(String name) {
    return loadTestFile("src/test/resources/report_" + name + ".json");
  }

}
