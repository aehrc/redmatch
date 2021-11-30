/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

/**
 * Represents a field and its corresponding label.
 *
 * @author Alejandro Metke Jimenez
 */
public class LabeledField {
  private final String id;
  private final String label;

  public LabeledField(String id, String label) {
    this.id = id;
    this.label = label;
  }

  public LabeledField(Field field) {
    this(field.getFieldId(), field.getLabel());
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

}
