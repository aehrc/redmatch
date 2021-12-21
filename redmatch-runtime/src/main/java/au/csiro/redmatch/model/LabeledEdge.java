/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import org.jgrapht.graph.DefaultEdge;

/**
 * A labeled edge.
 *
 * @author Alejandro Metke Jimenez
 */
public class LabeledEdge extends DefaultEdge {
  private final String label;

  public LabeledEdge(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
