/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model;

import javax.persistence.*;

/**
 * Represents a rule validation error.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Entity
public class Annotation {

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Long id;

  @ManyToOne
  private RedmatchProject project;

  /**
   * The line where the problem starts.
   */
  private int rowStart;

  /**
   * The position in the line where the problem starts.
   */
  private int colStart;
  
  /**
   * The line where the problem ends.
   */
  private int rowEnd;

  /**
   * The position in the line where the problem ends.
   */
  private int colEnd;

  /**
   * The message with the error details.
   */
  private String text;

  
  /**
   * The type of annotation.
   */
  private AnnotationType annotationType;

  public Annotation() {

  }
  
  /**
   * Creates a new rule validation error.
   * 
   * @param rowStart The line where the error starts.
   * @param colStart The position in the start line where the error starts.
   * @param rowEnd The line where the error ends.
   * @param colEnd The position in the start line where the error ends.
   * @param text The error message.
   */
  public Annotation(int rowStart, int colStart, int rowEnd, int colEnd, String text, 
      AnnotationType annotationType) {
    super();
    this.rowStart = rowStart;
    this.colStart = colStart;
    this.rowEnd = rowEnd;
    this.colEnd = colEnd;
    this.text = text;
    this.annotationType = annotationType;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public int getRowStart() {
    return rowStart;
  }

  public void setRowStart(int row) {
    this.rowStart = row;
  }

  public int getColStart() {
    return colStart;
  }

  public void setColStart(int col) {
    this.colStart = col;
  }
  
  public int getRowEnd() {
    return rowEnd;
  }

  public void setRowEnd(int row) {
    this.rowEnd = row;
  }

  public int getColEnd() {
    return colEnd;
  }

  public void setColEnd(int col) {
    this.colEnd = col;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public AnnotationType getAnnotationType() {
    return annotationType;
  }

  public void setAnnotationType(AnnotationType annotationType) {
    this.annotationType = annotationType;
  }

  public void setProject(RedmatchProject project) {
    this.project = project;
  }

  @Override
  public String toString() {
    return (annotationType != null ? annotationType.toString() : "NA") + " [" + rowStart + ", " + colStart + "]["
            + rowEnd + "," + colEnd +  "]: " + text;
  }

}