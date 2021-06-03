/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.importer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.csiro.redmatch.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.csiro.redmatch.client.IRedcapClient;
import au.csiro.redmatch.exceptions.RedmatchException;
import au.csiro.redmatch.model.Field.FieldType;
import au.csiro.redmatch.model.Field.TextValidationType;

/**
 * Imports Redcap projects into out internal model.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Component
public class RedcapImporter {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedcapImporter.class);
  
  @Autowired
  private Gson gson;

  private static final String SELECT_CHOICES_OR_CALCULATIONS = "select_choices_or_calculations";

  private static final String FIELD_TYPE = "field_type";

  private static final String FIELD_TEXT_VALIDATION = "text_validation_type_or_show_slider_number";

  private static final String FIELD_NAME = "field_name";

  private static final String FIELD_LABEL = "field_label";
  
  private IRedcapClient redcapClient;
  
  @Autowired
  public RedcapImporter(@Qualifier("redcap") IRedcapClient redcapClient) {
    this.redcapClient = redcapClient;
  }
  
  public void setRedcapClient(IRedcapClient redcapClient) {
    this.redcapClient = redcapClient;
  }

  /**
   * Adds the REDCap metadata to a Redmatch project.
   *
   * @param fieldIds The fields to return.
   * @param project The Redmatch project where the metadata will be added.
   */
  public void addMetadata(Set<String> fieldIds, RedmatchProject project) {
    addMetadata(redcapClient.getMetadata(project.getRedcapUrl(), project.getToken(), fieldIds), project);
  }

  /**
   * Creates a {@link RedmatchProject} from the REDCap metadata.
   * 
   * @param metadata The REDCap JSON metadata.
   * @param project The Redmatch project where the metadata will be added.
   */
  public void addMetadata(String metadata, RedmatchProject project) {
    // Get items from JSON
    Type listType = new TypeToken<List<HashMap<String, String>>>() {}.getType();
    List<Map<String, String>> meta = gson.fromJson(metadata, listType);
    if (meta.isEmpty()) {
      throw new RuntimeException("Metadata is empty!");
    }

    for (Map<String, String> entry : meta) {
      final String fieldName = entry.get(FIELD_NAME);
      
      Field field = new Field();
      field.setFieldLabel(entry.get(FIELD_LABEL));
      field.setFieldId(fieldName);
      try {
        field.setFieldType(
            FieldType.valueOf(entry.get(FIELD_TYPE).toUpperCase()));
      } catch (IllegalArgumentException e) {
        log.warn("Unknown field type: " + entry.get(FIELD_TYPE).toUpperCase());
        field.setFieldType(FieldType.UNKNOWN);
      }
      try {
        String textValidationType = entry.get(FIELD_TEXT_VALIDATION);
        if (!textValidationType.isEmpty()) {
          field.setTextValidationType(TextValidationType.valueOf(textValidationType.toUpperCase()));
        } else {
          // The FHIR_TERMINOLOGY validation type comes from the select_choices_or_calculations 
          // field
          String selectChoices = entry.get(SELECT_CHOICES_OR_CALCULATIONS);
          if (selectChoices != null && selectChoices.startsWith("FHIR:")) {
            field.setTextValidationType(TextValidationType.FHIR_TERMINOLOGY);
          }
        }
      } catch (IllegalArgumentException e) {
        log.warn("Unknown text validation type in field "  + fieldName + ": " 
            + entry.get(FIELD_TEXT_VALIDATION).toUpperCase());
        field.setTextValidationType(TextValidationType.NONE);
      }

      // Flatten choices - create a field element for each
      if (entry.containsKey(SELECT_CHOICES_OR_CALCULATIONS)) {
        final String select = entry.get(SELECT_CHOICES_OR_CALCULATIONS);
        if (select != null && !select.isEmpty() && select.contains("|")) {
          String[] parts = select.split("[|]");
          for (String part : parts) {

            int index = part.indexOf(',');
            if (index == -1) {
              log.warn("Invalid select choice '"+ part +"' in field " + fieldName 
                  + " (choices are: " + select + "). Ignoring.");
              continue;
            }

            final String code = part.substring(0, index).trim();
            final String label = part.substring(index + 1).trim();

            final Field f = new Field();
            f.setFieldId(fieldName + "___" + code);
            f.setFieldLabel(label);
            if ((field.getFieldType().equals(FieldType.CHECKBOX))) {
              f.setFieldType(FieldType.CHECKBOX_OPTION);
            } else if (field.getFieldType().equals(FieldType.RADIO) 
                || field.getFieldType().equals(FieldType.DROPDOWN)) {
              f.setFieldType(FieldType.DROPDOW_OR_RADIO_OPTION);
            }
            f.setTextValidationType(TextValidationType.NONE);
            project.addField(f);
          }
        }
      }
      project.addField(field);
    }
  }
  
  /**
   * Parses a list of name - value pairs in JSON format into a list of {@link Row}s.
   * 
   * @param data The JSON list.
   * @return The list of {@link Row}s.
   */
  public List<Row> parseData(String data) {
    Type listType = new TypeToken<List<HashMap<String, String>>>() {}.getType();
    final List<Map<String, String>> rows = gson.fromJson(data, listType);
    
    final List<Row> res = new ArrayList<>();
    
    for (Map<String, String> row : rows) {
      Row entry = new Row();
      entry.setData(row);
      res.add(entry);
    }
    return res;
  }
  
  /**
   * Returns the data in a report.
   * 
   * @param url REDCap URL.
   * @param token The API token.
   * @param reportId The report id.
   * @return The rows in the report.
   */
  public List<Row> getReport(String url, String token, String reportId) {
    return parseData(redcapClient.getReport(url, token, reportId));
  }
}
