/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.importer;

import au.csiro.redmatch.model.Field;
import au.csiro.redmatch.model.Schema;
import au.csiro.redmatch.util.FileUtils;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for REDCap schema importers.
 *
 * @author Alejandro Metke-Jimenez
 */
public class RedcapImporterTest {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedcapImporterTest.class);

  @Test
  public void testRedcapSchemaImport() {
    log.info("Running testRedcapSchemaImport");
    String jsonSchema = FileUtils.loadTextFileFromClassPath("schemaTutorial.json");
    RedcapJsonImporter jsonImporter = new RedcapJsonImporter(new Gson());
    Schema s1 = jsonImporter.loadSchema(jsonSchema);

    String csvSchema = FileUtils.loadTextFileFromClassPath("RedmatchTutorial_DataDictionary_2021-09-14.csv");
    RedcapCsvImporter csvImporter = new RedcapCsvImporter();
    Schema s2 = csvImporter.loadSchema(csvSchema);

    // Both schemas should be the same
    assertEquals(s1.getSchemaType(), s2.getSchemaType());
    assertEquals(s1.getFields().size(), s2.getFields().size());

    for (int i = 0; i < s1.getFields().size(); i++) {
      Field f1 = s1.getFields().get(i);
      Field f2 = s2.getFields().get(i);

      assertEquals(f1.getFieldId(), f2.getFieldId());
      assertEquals(f1.getType(), f2.getType());
    }
  }

}
