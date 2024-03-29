/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

/**
 * A node in a graph.
 *
 * @author Alejandro Metke Jimenez
 *
 */
public class D3Node {
  private String id;

  public D3Node() {

  }

  public D3Node(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "Node{" +
      "id='" + id + '\'' +
      '}';
  }
}
