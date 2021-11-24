/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import java.util.List;

/**
 * A single condition expression. These can be combined using {@link ConditionNode}s.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class ConditionExpression extends Condition {

  public enum ConditionExpressionOperator {
    EQ, NEQ, LT, GT, LTE, GTE
  }

  public enum ConditionType {
    TRUE, FALSE, NULL, NOTNULL, EXPRESSION
  }

  private ConditionType conditionType;

  private String fieldId;

  private ConditionExpressionOperator operator;

  private String stringValue;

  private Double numericValue;
  
  private Integer intValue;
  
  //private Coding codingValue;

  private List<String> resourceIds;

  /**
   * Creates a condition expression of type "EXPRESSION", which indicates that the rules should be run if the expression
   * evaluates to true.
   * 
   * @param fieldId The REDCap field.
   * @param operator The operator used in the evaluation.
   * @param stringValue The literal value used in the evaluation.
   */
  public ConditionExpression(String fieldId, ConditionExpressionOperator operator,
      String stringValue) {
    super();
    this.fieldId = fieldId;
    this.operator = operator;
    this.stringValue = stringValue;
    this.conditionType = ConditionType.EXPRESSION;
  }

  /**
   * Creates a condition expression of type "EXPRESSION", which indicates that the rules should be run if the expression
   * evaluates to true.
   * 
   * @param fieldId The REDCap field.
   * @param operator The operator used in the evaluation.
   * @param numericValue The literal value used in the evaluation.
   */
  public ConditionExpression(String fieldId, ConditionExpressionOperator operator,
      Double numericValue) {
    super();
    this.fieldId = fieldId;
    this.operator = operator;
    this.numericValue = numericValue;
    this.conditionType = ConditionType.EXPRESSION;
  }
  
  /**
   * Creates a condition expression of type "EXPRESSION", which indicates that the rules should be run if the expression
   * evaluates to true.
   * 
   * @param fieldId The REDCap field.
   * @param operator The operator used in the evaluation.
   * @param intValue The literal value used in the evaluation.
   */
  public ConditionExpression(String fieldId, ConditionExpressionOperator operator,
      Integer intValue) {
    super();
    this.fieldId = fieldId;
    this.operator = operator;
    this.intValue = intValue;
    this.conditionType = ConditionType.EXPRESSION;
  }

  /**
   * Creates a condition expression of type "NOTNULL", which indicates that the rules should be run if the referenced
   * fields has a value.
   * 
   * @param fieldId The id of the field.
   * @param isNull Indicates if this should be a NULL or NOTNULL expression.
   */
  public ConditionExpression(String fieldId, boolean isNull) {
    super();
    this.fieldId = fieldId;
    if (isNull) {
      this.conditionType = ConditionType.NULL;
    } else {
      this.conditionType = ConditionType.NOTNULL;
    }
  }

  /**
   * Creates a condition expression of type "TRUE" or "FALSE".
   * 
   * @param booleanValue The boolean value of the condition expression.
   */
  public ConditionExpression(boolean booleanValue) {
    super();
    if (booleanValue) {
      this.conditionType = ConditionType.TRUE;
    } else {
      this.conditionType = ConditionType.FALSE;
    }
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public Double getNumericValue() {
    return numericValue;
  }

  public void setNumericValue(Double numericValue) {
    this.numericValue = numericValue;
  }

  public Integer getIntValue() {
    return intValue;
  }

  public void setIntValue(Integer intValue) {
    this.intValue = intValue;
  }

  public ConditionType getConditionType() {
    return conditionType;
  }

  public void setConditionType(ConditionType conditionType) {
    this.conditionType = conditionType;
  }

  public ConditionExpressionOperator getOperator() {
    return operator;
  }

  public void setOperator(ConditionExpressionOperator operator) {
    this.operator = operator;
  }

  public List<String> getResourceIds() {
    return resourceIds;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    switch (conditionType) {
      case EXPRESSION:
        sb.append("VALUE(");
        sb.append(fieldId);
        sb.append(")");
        switch (operator) {
          case EQ:
            sb.append(" = ");
            break;
          case GT:
            sb.append(" > ");
            break;
          case GTE:
            sb.append(" >= ");
            break;
          case LT:
            sb.append(" < ");
            break;
          case LTE:
            sb.append(" <= ");
            break;
          case NEQ:
            sb.append(" != ");
            break;
          default:
            throw new RuntimeException("Unexpected operator. This should never happen!");
        }
        if (intValue != null) {
          sb.append(intValue.toString());
        } else if (numericValue != null) {
          sb.append(numericValue.toString());
        } else if (stringValue != null) {
          sb.append(stringValue.toString());
        } else {
          sb.append("null");
        }
        break;
      case FALSE:
        sb.append("FALSE");
        break;
      case TRUE:
        sb.append("TRUE");
        break;
      case NOTNULL:
        sb.append("NOTNULL(");
        sb.append(fieldId);
        sb.append(")");
        break;
      case NULL:
        sb.append("NULL(");
        sb.append(fieldId);
        sb.append(")");
        break;
      default:
        throw new RuntimeException("Unexpected condition type " + conditionType);
    }

    return sb.toString();
  }

  @Override
  public DataReference referencesData() {
    if (conditionType.equals(ConditionType.TRUE) || conditionType.equals(ConditionType.FALSE)) {
      return DataReference.NO;
    }
    return DataReference.YES;
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

}
