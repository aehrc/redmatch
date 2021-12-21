/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A graph representation that is understood by D3 when serialised in JSON format.
 *
 * @author Alejandro Metke Jimenez
 *
 */
public class D3Graph {

  private List<D3Node> d3Nodes = new ArrayList<>();
  private List<D3Link> d3Links = new ArrayList<>();
  private Set<String> types = new HashSet<>();

  public D3Graph() {

  }

  public List<D3Node> getNodes() {
    return d3Nodes;
  }

  public void setNodes(List<D3Node> d3Nodes) {
    this.d3Nodes = d3Nodes;
  }

  public List<D3Link> getLinks() {
    return d3Links;
  }

  public void setLinks(List<D3Link> d3Links) {
    this.d3Links = d3Links;
  }

  public Set<String> getTypes() {
    return types;
  }

  public void setTypes(Set<String> types) {
    this.types = types;
  }

  public void addNode(D3Node d3Node) {
    d3Nodes.add(d3Node);
  }

  public void addLink(D3Link d3Link) {
    d3Links.add(d3Link);
    types.add(d3Link.getType());
  }

  @Override
  public String toString() {
    return "Graph{" +
      "nodes=" + d3Nodes +
      ", links=" + d3Links +
      ", types=" + types +
      '}';
  }
}
