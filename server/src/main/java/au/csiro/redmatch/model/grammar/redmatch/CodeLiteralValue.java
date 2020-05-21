/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
 */
package au.csiro.redmatch.model.grammar.redmatch;


/**
 * A code literal.
 * 
 * @author Alejandro Metke
 *
 */
public class CodeLiteralValue extends Value {

  private String code;
  
  public CodeLiteralValue(String code) {
    super();
    this.code = code;
  }

  public String getCode() {
    return code;
  }
  public void setCode(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return "CodeLiteralValue [code=" + code + "]";
  }

}
