/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.importer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.csiro.redmatch.exceptions.RedmatchException;
import au.csiro.redmatch.model.Field;
import au.csiro.redmatch.model.Mapping;
import au.csiro.redmatch.model.Metadata;
import au.csiro.redmatch.model.Row;
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

  /**
   * Returns the project id of a REDCap project.
   * 
   * @param projectInfo The JSON string with the project info.
   * @return The project id.
   */
  public String getProjectId(String projectInfo) {
    return parseProjectInfo(projectInfo).get("project_id");
  }

  /**
   * Returns a map with the name-value pairs in the project info.
   * 
   * @param projectInfo The JSON string with the project info.
   * @return The project info as a map of name value pairs.
   */
  public Map<String, String> parseProjectInfo(String projectInfo) {
    final Type mapType = new TypeToken<HashMap<String, String>>() {}.getType();
    return gson.fromJson(projectInfo, mapType);
  }
  
  /**
   * Creates a {@link Metadata} from the REDCap metadata.
   * 
   * @param metadata The REDCap JSON metadata.
   * 
   * @return The metadata.
   */
  public Metadata parseMetadata(String metadata) {
    final Metadata res = new Metadata();
    
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
        }
      } catch (IllegalArgumentException e) {
        log.warn("Unknown text validation type: " + entry.get(FIELD_TEXT_VALIDATION).toUpperCase());
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
              throw new RedmatchException("Invalid select choices: " + select);
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
            res.getFields().add(f);
          }
        }
      }
      res.getFields().add(field);
    }

    return res;
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
   * Generates the mappings from the metadata.
   * 
   * @param metadata The internal object that represents the REDCap metadata.
   * @param fieldIds The map of the ids of the fields defined in the transformation rules and a 
   * boolean value that indicates if they are required. A field requires mapping when it is part of 
   * the "resource" section of the rule and uses the CONCEPT_SELECTED or CODE_SELECTED keyword.
   * 
   * @return The mappings for the fields defined in the rules. If the fields in the rules reference
   *         fields that do not exist in REDCap then these will be ignored.
   */
  public List<Mapping> generateMappings(Metadata metadata, Map<String, Boolean> fieldIds) {
    log.info("Generating mappings");
    if (fieldIds.isEmpty()) {
      return Collections.emptyList(); // Optimisation
    }

    final List<Mapping> res = new ArrayList<>();
    final List<Field> fields = metadata.getFields();
    log.debug("Processing " + fields.size() + " fields.");
    for (Field field : fields) {
      String redcapFieldId = field.getFieldId();
      log.debug("Processing field " + redcapFieldId);
      if (fieldIds.containsKey(redcapFieldId) && fieldIds.get(redcapFieldId).booleanValue()) {
        log.debug("Will create mappings for field " + field);
        String label = field.getFieldLabel();
        FieldType fieldType = field.getFieldType();
        Mapping mapping = new Mapping();
        mapping.setRedcapFieldId(redcapFieldId);
        mapping.setRedcapFieldType(fieldType.toString());
        mapping.setRedcapLabel(label);
        res.add(mapping);
      }
    }
    
    return res;
  }

}
