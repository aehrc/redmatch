/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a mapping rule from the source schema to a FHIR resource.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class Rule extends GrammarObject {
  
  /**
   * The condition needed to trigger the rule.
   */
  private Condition condition;

  /**
   * The main body.
   */
  private Body body;
  
  /**
   * An optional else body.
   */
  private Body elseBody;

  /**
   * The row in the rules document where this rule starts.
   */
  private final int rowStart;
  
  /**
   * The column in the rules document where this rule starts.
   */
  private final int colStart;
  
  /**
   * The row in the rules document where this rule end.
   */
  private final int rowEnd;
  
  /**
   * The column in the rules document where this rule ends.
   */
  private final int colEnd;

  /**
   * Constructor.
   * 
   * @param rowStart The row where the rule starts.
   * @param colStart The column where the rule starts.
   * @param rowEnd The row where the rule ends.
   * @param colEnd The column where the rule ends.
   */
  public Rule(int rowStart, int colStart, int rowEnd, int colEnd) {
    this.rowStart = rowStart;
    this.colStart = colStart;
    this.rowEnd = rowEnd;
    this.colEnd = colEnd;
  }

  public Condition getCondition() {
    return condition;
  }

  public void setCondition(Condition condition) {
    this.condition = condition;
  }
  
  public int getRowStart() {
    return rowStart;
  }

  public int getColStart() {
    return colStart;
  }

  public int getRowEnd() {
    return rowEnd;
  }

  public int getColEnd() {
    return colEnd;
  }

  public Body getBody() {
    return body;
  }

  public void setBody(Body body) {
    this.body = body;
  }

  public Body getElseBody() {
    return elseBody;
  }

  public void setElseBody(Body elseBody) {
    this.elseBody = elseBody;
  }
  
  /**
   * Returns all the {@link Resource}s that are nested in this rule.
   * 
   * @return Resources nested in this rule.
   */
  public Collection<Resource> getResources() {
    final Collection<Resource> res = new ArrayList<>();
    if (body != null) {
      res.addAll(body.getResources());
      for (Rule r : body.getRules()) {
        res.addAll(r.getResources());
      }
    }
    if(elseBody != null) {
      res.addAll(elseBody.getResources());
      for (Rule r : elseBody.getRules()) {
        res.addAll(r.getResources());
      }
    }
    
    return res;
  }
  
  /**
   * Returns all {@link Condition}s in this rule, including nested ones.
   * 
   * @return Conditions in this rule.
   */
  public Collection<Condition> getConditions() {
    final Collection<Condition> res = new ArrayList<>();
    res.add(condition);
    
    if (body != null) {
      for (Rule r : body.getRules()) {
        res.addAll(r.getConditions());
      }
    }
    if(elseBody != null) {
      for (Rule r : elseBody.getRules()) {
        res.addAll(r.getConditions());
      }
    }
    
    return res;
  }

  /**
   * Returns all {@link ConditionExpression}s in this rule, including nested ones.
   *
   * @return ConditionExpressions in this rule.
   */
  public Collection<ConditionExpression> getConditionExpressions() {
    final Collection<ConditionExpression> res = new ArrayList<>();
    getConditionExpressionRecursive(condition, res);
    return res;
  }

  private void getConditionExpressionRecursive (Condition condition, Collection<ConditionExpression> res) {
    if (condition instanceof ConditionExpression) {
      res.add((ConditionExpression) condition);
    } else if (condition instanceof ConditionNode){
      ConditionNode node = (ConditionNode) condition;
      getConditionExpressionRecursive(node.getLeftCondition(), res);
      getConditionExpressionRecursive(node.getRightCondition(), res);
    } else {
      throw new RuntimeException("Unexpected condition type: " + condition.getClass());
    }
  }
  
  /**
   * Indicates if this rule references any fields in the REDCap form. For example, the rule 
   * <b>TRUE { Observation<o> : * status = CODE_LITERAL(final) }</b> does not reference any elements
   * in the data and therefore should only generate a single resource, not a resource per row.
   * 
   * @return True if the rule references any form fields (including the condition), or false 
   * otherwise.
   */
  public DataReference referencesData() {
    if (condition.referencesData().equals(DataReference.YES) 
        || body.referencesData().equals(DataReference.YES) 
        || (elseBody != null && elseBody.referencesData().equals(DataReference.YES))) {
      return DataReference.YES;
    } else if (condition.referencesData().equals(DataReference.RESOURCE) 
        || body.referencesData().equals(DataReference.RESOURCE) 
        || (elseBody != null && elseBody.referencesData().equals(DataReference.RESOURCE))) {
      return DataReference.RESOURCE;
    } else {
      return DataReference.NO;
    }
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime * result + colEnd;
    result = prime * result + colStart;
    result = prime * result + ((condition == null) ? 0 : condition.hashCode());
    result = prime * result + ((elseBody == null) ? 0 : elseBody.hashCode());
    result = prime * result + rowEnd;
    result = prime * result + rowStart;
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
    Rule other = (Rule) obj;
    if (body == null) {
      if (other.body != null)
        return false;
    } else if (!body.equals(other.body))
      return false;
    if (colEnd != other.colEnd)
      return false;
    if (colStart != other.colStart)
      return false;
    if (condition == null) {
      if (other.condition != null)
        return false;
    } else if (!condition.equals(other.condition))
      return false;
    if (elseBody == null) {
      if (other.elseBody != null)
        return false;
    } else if (!elseBody.equals(other.elseBody))
      return false;
    if (rowEnd != other.rowEnd)
      return false;
    return rowStart == other.rowStart;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    if (condition != null) {
      sb.append(condition);
    }

    if (body != null) {
      sb.append(body);
    }
    
    if (elseBody != null) {
      sb.append(" ELSE ");
      sb.append(elseBody);
    }
    return sb.toString();
  }

  public String toStringShort() {
    return toString().substring(0, 50) + "...";
  }

}
