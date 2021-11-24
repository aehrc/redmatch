/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

/**
 * Represents a repeats clause, used to simplify dealing with multiple REDCap fields used to implement lists.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class RepeatsClause extends GrammarObject {
  
  private int start;
  
  private int end;
  
  private String varName;
  
  /**
   * Constructor.
   */
  public RepeatsClause() {
    
  }
  
  /**
   * Constructor.
   * 
   * @param start The start index.
   * @param end The end index.
   * @param varName The name of the variable.
   */
  public RepeatsClause(int start, int end, String varName) {
    super();
    this.start = start;
    this.end = end;
    this.varName = varName;
  }

  /**
   * @return the start
   */
  public int getStart() {
    return start;
  }

  /**
   * @param start the start to set
   */
  public void setStart(int start) {
    this.start = start;
  }

  /**
   * @return the end
   */
  public int getEnd() {
    return end;
  }

  /**
   * @param end the end to set
   */
  public void setEnd(int end) {
    this.end = end;
  }

  /**
   * @return the varName
   */
  public String getVarName() {
    return varName;
  }

  /**
   * @param varName the varName to set
   */
  public void setVarName(String varName) {
    this.varName = varName;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + end;
    result = prime * result + start;
    result = prime * result + ((varName == null) ? 0 : varName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RepeatsClause other = (RepeatsClause) obj;
    if (end != other.end)
      return false;
    if (start != other.start)
      return false;
    if (varName == null) {
      return other.varName == null;
    } else return varName.equals(other.varName);
  }

  @Override
  public String toString() {
    return "<" + start + ".." + end + ": " + varName + ">";
  }

  @Override
  public DataReference referencesData() {
    return DataReference.NO;
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

}
