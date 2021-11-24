package au.csiro.redmatch.importer;

import au.csiro.redmatch.model.Schema;

import java.io.File;

/**
 * Defines the functionality of a schema importer.
 *
 * @author Alejandro Metke-Jimenez
 */
public interface SchemaImporter {

  /**
   * Loads a schema from a file.
   *
   * @param schemaFile The schema file.
   * @return The schema.
   */
  Schema loadSchema(File schemaFile);

  /**
   * Loads a schema from a string.
   *
   * @param schemaString The schema string.
   * @return The schema.
   */
  Schema loadSchema(String schemaString);
}
