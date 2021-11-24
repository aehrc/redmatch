/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

/**
 * Represents a node in a tree of conditions.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class ConditionNode extends Condition {

  public enum ConditionNodeOperator {
    AND, OR
  }

  /**
   * The operator for this node.
   */
  private final ConditionNodeOperator op;

  /**
   * The left condition for this node.
   */
  private final Condition leftCondition;

  /**
   * The right condition for this node.
   */
  private final Condition rightCondition;
  
  /**
   * Creates a new condition node.
   * 
   * @param leftCondition The left condition.
   * @param op The operator.
   * @param rightCondition The right condition.
   */
  public ConditionNode(Condition leftCondition, ConditionNodeOperator op,
      Condition rightCondition) {
    this.leftCondition = leftCondition;
    this.op = op;
    this.rightCondition = rightCondition;
  }

  public ConditionNodeOperator getOp() {
    return op;
  }

  public Condition getLeftCondition() {
    return leftCondition;
  }

  public Condition getRightCondition() {
    return rightCondition;
  }

  @Override
  public String toString() {
    return leftCondition + " " + op + " " + rightCondition;
  }

  @Override
  public DataReference referencesData() {
    if (leftCondition.referencesData().equals(DataReference.YES) 
        || rightCondition.referencesData().equals(DataReference.YES)) {
      return DataReference.YES;
    } else if (leftCondition.referencesData().equals(DataReference.RESOURCE) 
        || rightCondition.referencesData().equals(DataReference.RESOURCE)) {
      return DataReference.RESOURCE;
    } else {
      return DataReference.NO;
    }
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

}
