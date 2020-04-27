/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * A concept literal. This has the form system | code | display.
 * 
 * @author Alejandro Metke
 *
 */
public class ConceptLiteralValue extends Value {
  
  private String system;
  private String code;
  private String display;
  
  public ConceptLiteralValue(String system, String code, String display) {
    super();
    this.system = system;
    this.code = code;
    this.display = display;
  }
  public ConceptLiteralValue(String system, String code) {
    super();
    this.system = system;
    this.code = code;
  }
  public String getSystem() {
    return system;
  }
  public void setSystem(String system) {
    this.system = system;
  }
  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }
  public String getDisplay() {
    return display;
  }
  public void setDisplay(String display) {
    this.display = display;
  }
  @Override
  public String toString() {
    return system + "|" + code + display != null ? ("|" + display) : "";
  }
  
}
