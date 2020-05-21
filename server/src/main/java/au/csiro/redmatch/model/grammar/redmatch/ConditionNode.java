/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.model.grammar.redmatch;

import java.util.Map;

import au.csiro.redmatch.model.Metadata;

/**
 * Represents a node in a tree of conditions.
 * 
 * @author Alejandro Metke Jimenez
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

  private boolean doEvaluate(Metadata metadata, Map<String, String> data) {
    switch (op) {
      case AND:
        return leftCondition.evaluate(metadata, data)
            && rightCondition.evaluate(metadata, data);
      case OR:
        return leftCondition.evaluate(metadata, data)
            || rightCondition.evaluate(metadata, data);
      default:
        throw new RuntimeException("Unexpected condition node operator. This should never happen!");
    }
  }

  @Override
  public boolean evaluate(Metadata metadata, Map<String, String> data) {
    if (negated) {
      return !doEvaluate(metadata, data);
    } else {
      return doEvaluate(metadata, data);
    }
  }

  @Override
  public String toString() {
    return leftCondition + " " + op + " " + rightCondition;
  }

}
