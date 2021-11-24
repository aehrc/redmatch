/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.importer;

import au.csiro.redmatch.model.RedcapField;
import au.csiro.redmatch.model.Schema;
import au.csiro.redmatch.util.FileUtils;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports a REDCap schema in CSV format into our internal model.
 *
 * @author Alejandro Metke-Jimenez
 */
public class RedcapCsvImporter implements SchemaImporter {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedcapCsvImporter.class);

  @Override
  public Schema loadSchema(File schemaFile) {
    String schemaString = FileUtils.loadTextFile(schemaFile);
    return loadSchema(schemaString);
  }

  @Override
  public Schema loadSchema(String schemaString) {

    Schema res = new Schema(Schema.SchemaType.REDCAP);

    // This assumes the order of the REDCap schema in CSV format is stable
    // A 0 - "Variable / Field Name",
    // B 1 - "Form Name",
    // C 2 - "Section Header",
    // D 3 - "Field Type",
    // E 4 - "Field Label",
    // F 5 - "Choices, Calculations, OR Slider Labels",
    // G 6 - "Field Note",
    // H 7 - "Text Validation Type OR Show Slider Number",
    // I 8 - "Text Validation Min",
    // J 9 - "Text Validation Max",
    // K 10  - Identifier?,
    // L 11 - "Branching Logic (Show field only if...)",
    // M 12 - "Required Field?",
    // N 13 - "Custom Alignment",
    // O 14 - "Question Number (surveys only)",
    // P 15 - "Matrix Group Name",
    // Q 16 - "Matrix Ranking?",
    // R 17 - "Field Annotation"
    for (String[] schemaRow : loadCsv(schemaString)) {
      final String fieldId = schemaRow[0];
      final String fieldLabel = schemaRow[4];
      RedcapField.TextValidationType textValidationType;
      RedcapField field;

      // Try to load text validation first. If this is set then the field is of type TEXT.
      String val = schemaRow[7];
      if (!isEmpty(val)) {
        try {
          textValidationType = RedcapField.TextValidationType.valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
          log.warn("Unknown text validation type in field "  + fieldId + ": " + schemaRow[7].toUpperCase());
          textValidationType = RedcapField.TextValidationType.NONE;
        }
        field = new RedcapField(fieldId, fieldLabel, textValidationType);
      } else {
        // Determine if field is using FHIR Terminology Plugin
        String selectChoices = schemaRow[5];
        if (!isEmpty(selectChoices) && selectChoices.startsWith("FHIR:")) {
          field = new RedcapField(fieldId, fieldLabel, RedcapField.TextValidationType.FHIR_TERMINOLOGY);
        } else {
          try {
            field = new RedcapField(fieldId, fieldLabel, RedcapField.FieldType.valueOf(schemaRow[3].toUpperCase()));
          } catch (IllegalArgumentException e) {
            log.warn("Unknown field type: " + schemaRow[3].toUpperCase());
            field = new RedcapField(fieldId, fieldLabel,  RedcapField.FieldType.UNKNOWN);
          }
        }
      }
      res.getFields().add(field);

      // Flatten choices - create a field element for each
      String select = schemaRow[5];
      if (!isEmpty(select)) {
        if (select.contains("|")) {
          String[] parts = select.split("[|]");
          for (String part : parts) {
            int index = part.indexOf(',');
            if (index == -1) {
              log.warn("Invalid select choice '"+ part +"' in field " + fieldId
                + " (choices are: " + select + "). Ignoring.");
              continue;
            }
            final String code = part.substring(0, index).trim();
            final String label = part.substring(index + 1).trim();
            if ((field.getFieldType().equals(RedcapField.FieldType.CHECKBOX))) {
              RedcapField rf = new RedcapField(fieldId + "___" + code, label, RedcapField.FieldType.CHECKBOX_OPTION);
              field.getOptions().add(rf);
              res.getFields().add(rf);
            } else if (field.getFieldType().equals(RedcapField.FieldType.RADIO)
              || field.getFieldType().equals(RedcapField.FieldType.DROPDOWN)) {
              RedcapField rf = new RedcapField(fieldId + "___" + code, label,
                RedcapField.FieldType.DROPDOW_OR_RADIO_OPTION);
              field.getOptions().add(rf);
              res.getFields().add(rf);
            } else {
              log.warn("Unexpected field type found while creating option fields: " + field.getFieldType());
            }
          }
        }
      }
    }

    return res;
  }

  private boolean isEmpty(String val) {
    if (val == null || val.isEmpty()) {
      return true;
    }
    return false;
  }

  private List<String[]> loadCsv(String schemaString) {
    try (StringReader reader = new StringReader(schemaString)) {
      CSVParser parser = new CSVParserBuilder()
        .withSeparator(',')
        .withIgnoreQuotations(false)
        .build();

      CSVReader csvReader = new CSVReaderBuilder(reader)
        .withSkipLines(1)
        .withCSVParser(parser)
        .build();

      List<String[]> list = new ArrayList<>();
      String[] line;
      while ((line = csvReader.readNext()) != null) {
        list.add(line);
      }
      reader.close();
      csvReader.close();
      return list;
    } catch (CsvValidationException e) {
      throw new ImportingException("The REDCap schema CSV file is malformed.", e);
    } catch (IOException e) {
      throw new ImportingException("There was a problem reading the REDCap schema CSV file.", e);
    }
  }
}
