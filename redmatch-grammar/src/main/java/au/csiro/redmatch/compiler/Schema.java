/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import au.csiro.redmatch.util.FileUtils;

import java.io.File;

/**
 * Represents a schema in the rules language document. A schema has a location and a type.
 *
 * @author Alejandro Metke-Jimenez
 */
public class Schema extends GrammarObject {

  private final File baseFolder;

  private final String schemaLocation;

  private final String schemaType;

  public Schema(File baseFolder, String schemaLocation, String schemaType) {
    this.baseFolder = baseFolder;
    this.schemaLocation = schemaLocation;
    this.schemaType = schemaType;
  }

  /**
   * Determines if the schema is valid, which means a file exists and can be read. Attempts the following:
   * <ol>
   *   <li>Tries to load the file using the value of schemaLocation if it is absolute.</li>
   *   <li>If it is not absolute then it tries to use baseFolder.</li>
   *   <li>If that doesn't succeed then it uses the default folder where the app was run from.</li>
   *   <li>If none of the above succeed then it tries to load the file from the classpath.</li>
   *   <li>If that doesn't work then it returns false to indicate that the schema could not be accessed.</li>
   * </ol>
   *
   * @return true if the schema is valid and can be accessed, false otherwise.
   */
  public boolean isValid() {
    File f = new File(schemaLocation);
    if (f.isAbsolute()) {
      return f.isFile() && f.canRead();
    } else {
      // File is not absolute so there are several things to try
      if (baseFolder != null) {
        // Use base folder if not null
        File af = new File(baseFolder, f.getName());
        if (af.isFile() && af.canRead()) {
          return true;
        }
      }

      // Use default folder where the app was run from
      if (f.isFile() && f.canRead()) {
        return true;
      }

      // If we get here then we haven't succeeded with any of the file-based attempts, so we can try to load from the
      // classpath
      try {
        FileUtils.loadFileFromClassPath(schemaLocation);
        return true;
      } catch(NullPointerException e) {
        return false;
      }
    }
  }

  public String getSchemaType() {
    return this.schemaType;
  }

  public String getSchemaLocation() {
    return schemaLocation;
  }

  /**
   * Returns the schema file.
   *
   * @return The schema file or null of the schema is not valid. This can be checked with the isValid method.
   */
  public File getSchema() {
    File f = new File(schemaLocation);
    if (f.isAbsolute()) {
      if(f.isFile() && f.canRead()) {
        return f;
      } else {
        return null;
      }
    } else {
      // File is not absolute so there are several things to try
      if (baseFolder != null) {
        // Use base folder if not null
        File af = new File(baseFolder, f.getName());
        if (af.isFile() && af.canRead()) {
          return af;
        }
      }

      // Use default folder where the app was run from
      if (f.isFile() && f.canRead()) {
        return f;
      }

      // If we get here then we haven't succeeded with any of the file-based attempts, so we can try to load from the
      // classpath
      try {
        return FileUtils.loadFileFromClassPath(schemaLocation);
      } catch(NullPointerException e) {
        return null;
      }
    }
  }

  @Override
  public DataReference referencesData() {
    return DataReference.NO;
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }
}
