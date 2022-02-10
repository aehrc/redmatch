/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import au.csiro.redmatch.model.VersionedFhirPackage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;

public class FileUtils {

  private static final Log log = LogFactory.getLog(FileUtils.class);

  /**
   * Loads a text file into a {@link String}.
   *
   * @param file The file.
   * @return The contents of the file.
   */
  public static String loadTextFile(File file) {
    String absolutePath = file.getAbsolutePath();
    log.debug("Loading test file from " + absolutePath);

    try (Scanner scanner = new Scanner(new File(absolutePath), StandardCharsets.UTF_8)) {
      return scanner.useDelimiter("\\A").next();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Loads a text file into a {@link String}.
   *
   * @param name The name of the file.
   * @return The contents of the file.
   */
  public static String loadTextFileFromClassPath(String name) {
    File file = loadFileFromClassPath(name);
    return loadTextFile(file);
  }

  /**
   * Loads a file from the class path.
   *
   * @param name The name of the file.
   * @return The corresponding {@link File}.
   * @throws NullPointerException If the file cannot be loaded from the class path.
   */
  public static File loadFileFromClassPath(String name) {
    ClassLoader classLoader = FileUtils.class.getClassLoader();
    return new File(Objects.requireNonNull(classLoader.getResource(name)).getFile());
  }

  /**
   * Loads a file from the class path as an {@link InputStream}.
   *
   * @param name The name of the file.
   * @return The corresponding {@link InputStream}.
   * @throws NullPointerException If the file cannot be loaded from the class path.
   */
  public static InputStream loadFileAsInputStream(String name) {
    ClassLoader classLoader = FileUtils.class.getClassLoader();
    return classLoader.getResourceAsStream(name);
  }

  /**
   * Returns the local path of a FHIR package.
   *
   * @param fhirPackage The FHIR package.
   * @return The local path.
   */
  public static Path getFolderForFhirPackage(VersionedFhirPackage fhirPackage) {
    Path userFolder = new File(System.getProperty("user.home")).toPath();
    Path fhirFolder = userFolder.resolve(".fhir");
    Path packagesFolder = fhirFolder.resolve("packages");
    Path packageFolder = packagesFolder.resolve(fhirPackage.toString());
    return packageFolder.resolve("package");
  }
}
