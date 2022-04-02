/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents all the data for a patient.
 */
public class PatientData {

  /**
   * The id of the patient.
   */
  private final String id;

  /**
   * A list of JSON objects that contains the rest of the patient data. The contents of each object depend on the
   * source. For example, if REDCap is the source and there are no repeatable instruments then the list will contain a
   * single object with all the patient data flattened out. If the project does contain repeatable instruments then the
   * list will contain an object for the non-repeatable data and additional objects for the instances of repeatable
   * instruments.
   */
  private final List<JsonObject> objects = new ArrayList<>();

  public PatientData(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public List<JsonObject> getObjects () {
    return Collections.unmodifiableList(objects);
  }

  public void addObject(JsonObject object) {
    objects.add(object);
  }

}
