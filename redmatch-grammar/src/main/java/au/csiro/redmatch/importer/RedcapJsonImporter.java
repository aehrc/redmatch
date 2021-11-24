/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.importer;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.csiro.redmatch.model.*;
import au.csiro.redmatch.util.FileUtils;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


/**
 * Imports a REDCap schema in JSON format into our internal model.
 * 
 * @author Alejandro Metke-Jimenez
 */
public class RedcapJsonImporter implements SchemaImporter {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedcapJsonImporter.class);

  private final Gson gson;

  private static final String SELECT_CHOICES_OR_CALCULATIONS = "select_choices_or_calculations";

  private static final String FIELD_TYPE = "field_type";

  private static final String FIELD_TEXT_VALIDATION = "text_validation_type_or_show_slider_number";

  private static final String FIELD_NAME = "field_name";

  private static final String FIELD_LABEL = "field_label";

  public RedcapJsonImporter() {
    gson = new Gson();
  }

  public RedcapJsonImporter(Gson gson) {
    this.gson = gson;
  }

  /**
   * Creates a {@link Schema} from the REDCap metadata.
   * 
   * @param schemaFile The REDCap JSON metadata file.
   * @return The schema.
   */
  @Override
  public Schema loadSchema(File schemaFile) {
    String schemaString = FileUtils.loadTextFile(schemaFile);
    return loadSchema(schemaString);
  }

  @Override
  public Schema loadSchema(String schemaString) {
    // Get items from JSON
    Type listType = new TypeToken<List<HashMap<String, String>>>() {}.getType();
    List<Map<String, String>> meta = null;
    try {
      meta = gson.fromJson(schemaString, listType);
    } catch (JsonSyntaxException e) {
      throw new ImportingException("There was a problem importing the JSON schema.", e);
    }
    if (meta.isEmpty()) {
      throw new RuntimeException("Metadata is empty!");
    }

    Schema res = new Schema(Schema.SchemaType.REDCAP);

    for (Map<String, String> entry : meta) {
      final String fieldId = entry.get(FIELD_NAME);
      final String fieldLabel = entry.get(FIELD_LABEL);
      RedcapField.TextValidationType textValidationType;
      RedcapField field;

      // Try to load text validation first. If this is set then the field is of type TEXT.
      String val = entry.get(FIELD_TEXT_VALIDATION);
      if (!val.isEmpty()) {
        try {
          textValidationType = RedcapField.TextValidationType.valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
          log.warn("Unknown text validation type in field "  + fieldId + ": "
            + entry.get(FIELD_TEXT_VALIDATION).toUpperCase());
          textValidationType = RedcapField.TextValidationType.NONE;
        }
        field = new RedcapField(fieldId, fieldLabel, textValidationType);
      } else {
        // Determine if field is using FHIR Terminology Plugin
        String selectChoices = entry.get(SELECT_CHOICES_OR_CALCULATIONS);
        if (selectChoices != null && selectChoices.startsWith("FHIR:")) {
          field = new RedcapField(fieldId, fieldLabel, RedcapField.TextValidationType.FHIR_TERMINOLOGY);
        } else {
          try {
            field = new RedcapField(fieldId, fieldLabel,
              RedcapField.FieldType.valueOf(entry.get(FIELD_TYPE).toUpperCase()));
          } catch (IllegalArgumentException e) {
            log.warn("Unknown field type: " + entry.get(FIELD_TYPE).toUpperCase());
            field = new RedcapField(fieldId, fieldLabel, RedcapField.FieldType.UNKNOWN);
          }
        }
      }

      res.getFields().add(field);

      // Flatten choices - create a field element for each
      if (entry.containsKey(SELECT_CHOICES_OR_CALCULATIONS)) {
        final String select = entry.get(SELECT_CHOICES_OR_CALCULATIONS);
        if (select != null && select.contains("|")) {
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

}
