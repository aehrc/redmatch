package au.csiro.redmatch.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a rule validation error.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Document
public class Annotation {

  @Id
  private String id;

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

  public String getId() {
    return id;
  }

  public void setId(String id) {
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

  @Override
  public String toString() {
    return annotationType.toString() + " [" + rowStart + ", " + colStart + "][" + rowEnd + "," 
        + colEnd +  "]: " + text;
  }

}