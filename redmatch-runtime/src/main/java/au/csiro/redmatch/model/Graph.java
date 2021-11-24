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
public class Graph {

  private List<Node> nodes = new ArrayList<>();
  private List<Link> links = new ArrayList<>();
  private Set<String> types = new HashSet<>();

  public Graph() {

  }

  public List<Node> getNodes() {
    return nodes;
  }

  public void setNodes(List<Node> nodes) {
    this.nodes = nodes;
  }

  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }

  public Set<String> getTypes() {
    return types;
  }

  public void setTypes(Set<String> types) {
    this.types = types;
  }

  public void addNode(Node node) {
    nodes.add(node);
  }

  public void addLink(Link link) {
    links.add(link);
    types.add(link.getType());
  }

  @Override
  public String toString() {
    return "Graph{" +
      "nodes=" + nodes +
      ", links=" + links +
      ", types=" + types +
      '}';
  }
}
