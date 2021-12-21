/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

/**
 * A link in a graph.
 *
 * @author Alejandro Metke Jimenez
 *
 */
public class D3Link {
  private String source;
  private String target;
  private String type;

  public D3Link() {

  }

  public D3Link(String source, String target, String type) {
    this.source = source;
    this.target = target;
    this.type = type;
  }

  public D3Link(String source, String target) {
    this(source, target, "UNKNOWN");
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "Link{" +
      "source='" + source + '\'' +
      ", target='" + target + '\'' +
      ", type='" + type + '\'' +
      '}';
  }
}
