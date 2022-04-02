/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import com.google.gson.JsonElement;
import org.jgrapht.Graph;

/**
 * Represents a row of data.
 *
 * When the data source is REDCap, the graph contains a node that represents the non-repeatable instruments. If the
 * project contains repeatable instruments then these are included as additional nodes, connected with an edge that has
 * the repeatable instruments name.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class Row {
  
  /**
   * A graph that represents all the information for a single patient.
   */
  private Graph<JsonElement, LabeledEdge> data = new LabeledDirectedMultigraph<>(LabeledEdge.class);

  /**
   * @return the data
   */
  public Graph<JsonElement, LabeledEdge> getData() {
    return data;
  }

  /**
   * @param data the data to set
   */
  public void setData(Graph<JsonElement, LabeledEdge> data) {
    this.data = data;
  }

}
