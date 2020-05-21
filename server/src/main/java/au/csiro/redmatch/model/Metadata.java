/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents the metadata of a REDCap project.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Document
public class Metadata {

  /**
   * The list of fields in the REDCap project.
   */
  private List<Field> fields = new ArrayList<>();

  public Metadata() {

  }

  public List<Field> getFields() {
    return fields;
  }

  public void setFields(List<Field> fields) {
    this.fields = fields;
  }
  
  public Field getField(String fieldId) {
    for (Field field : fields) {
      if (field.getFieldId().equals(fieldId)) {
        return field;
      }
    }
    return null;
  }
  
  public boolean hasField(String fieldId) {
    return getField(fieldId) != null;
  }

  /**
   * The unique field for each record is always the first field.
   * 
   * @return The unique field id.
   */
  public String getUniqueFieldId() {
    return getFields().get(0).getFieldId();
  }

  /**
   * Returns a set of all the field ids in this study. These are the names that can be referenced in
   * the transformation rules.
   * 
   * @return The set of field ids for this study.
   */
  public Set<String> getFieldIds() {
    final Set<String> fieldNames = new HashSet<>();
    for (Field field : getFields()) {
      final String fieldId = field.getFieldId();
      fieldNames.add(fieldId);
    }

    return fieldNames;
  }

}
