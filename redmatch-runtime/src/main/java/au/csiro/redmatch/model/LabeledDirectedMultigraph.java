/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import org.jgrapht.graph.DirectedMultigraph;

/**
 * A {@link DirectedMultigraph} that supports {@link LabeledEdge}s.
 *
 * @author Alejandro Metke Jimenez
 */
public class LabeledDirectedMultigraph<V, E> extends DirectedMultigraph<V, E> {

  public static final String VERTEX_TYPE_FIELD = "vertexType";

  public LabeledDirectedMultigraph(Class<? extends E> edgeClass) {
    super(edgeClass);
  }
}
