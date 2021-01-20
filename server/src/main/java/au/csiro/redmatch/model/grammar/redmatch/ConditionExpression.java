/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar.redmatch;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.csiro.redmatch.exporter.RuleApplicationException;
import au.csiro.redmatch.model.Field;
import au.csiro.redmatch.model.Metadata;
import au.csiro.redmatch.model.Field.FieldType;

/**
 * A single condition expression. These can be combined using {@link ConditionNode}s.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class ConditionExpression extends Condition {

  /** Logger. */
  private static final Log log = LogFactory.getLog(ConditionExpression.class);

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
   * Creates a condition expression of type "EXPRESSION", which indicates that the rules should be
   * run if the expression evaluates to true.
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
   * Creates a condition expression of type "EXPRESSION", which indicates that the rules should be
   * run if the expression evaluates to true.
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
   * Creates a condition expression of type "EXPRESSION", which indicates that the rules should be
   * run if the expression evaluates to true.
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
   * Creates a condition expression of type "NOTNULL", which indicates that the rules should be run
   * if the referenced fields has a value.
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

  private boolean doEvaluate(Metadata metadata, Map<String, String> data) {
    switch (conditionType) {
      case EXPRESSION:
        // 'VALUE' '(' variableIdentifier ')'('=' | '!=' | '<' | '>' | '<=' | '>=') (STRING | NUMBER)
        
        if (intValue == null && numericValue == null && stringValue == null) {
          throw new RuleApplicationException("No value has been specified for this expression. [" 
              + this.toString() + "]");
        }
        
        String actualStringValue = data.get(fieldId);
        if (actualStringValue == null || actualStringValue.isEmpty()) {
          
          // See if this is an option and extract the value from the name
          if (fieldId.contains("___")) {
            String[] parts = fieldId.split("___");
            String chosenVal = data.get(parts[0]);
            
            if (chosenVal != null && chosenVal.equals(parts[1])) {
              actualStringValue = "1";
            } else {
              actualStringValue = "0";
            }
          } else {
            // Otherwise the value is in fact missing
            log.warn("There was no value for field " + fieldId + " [" + this.toString() + "]");
            return false;
          }
        }

        // Get data type from REDCap metadata
        Field field = metadata.getField(this.fieldId);
        if (field == null) {
          throw new RuleApplicationException("No field " + this.fieldId + " found in metadata.");
        }
        
        if (numericValue != null) {
          try {
            final Double fieldValue = Double.parseDouble(actualStringValue);
            switch (operator) {
              case EQ:
                return fieldValue.doubleValue() == numericValue.doubleValue();
              case GT:
                return fieldValue.doubleValue() > numericValue.doubleValue();
              case GTE:
                return fieldValue.doubleValue() >= numericValue.doubleValue();
              case LT:
                return fieldValue.doubleValue() < numericValue.doubleValue();
              case LTE:
                return fieldValue.doubleValue() <= numericValue.doubleValue();
              case NEQ:
                return fieldValue.doubleValue() != numericValue.doubleValue();
              default:
                throw new RuntimeException("Unexpected operator. This should never happen!");
            }
          } catch (NumberFormatException e) {
            throw new RuleApplicationException("Could not parse value of field " + fieldId 
                + " into a number (" + data.get(fieldId) + ") [" + this.toString() + "]");
          }
        } else if (intValue != null) {
          try {
            final Integer fieldValue = Integer.parseInt(actualStringValue);
            switch (operator) {
              case EQ:
                return fieldValue.intValue() == intValue.intValue();
              case GT:
                return fieldValue.intValue() > intValue.intValue();
              case GTE:
                return fieldValue.intValue() >= intValue.intValue();
              case LT:
                return fieldValue.intValue() < intValue.intValue();
              case LTE:
                return fieldValue.intValue() <= intValue.intValue();
              case NEQ:
                return fieldValue.intValue() != intValue.intValue();
              default:
                throw new RuntimeException("Unexpected operator. This should never happen!");
            }
          } catch (NumberFormatException e) {
            throw new RuleApplicationException("Could not parse value of field " + fieldId 
                + " into an integer (" + data.get(fieldId) + ") [" + this.toString() + "]");
          }
        } else {
          // A string value
          switch (operator) {
          case EQ:
            return actualStringValue.equals(stringValue);
          case GT:
            return actualStringValue.compareTo(stringValue) > 0;
          case GTE:
            return actualStringValue.compareTo(stringValue) >= 0;
          case LT:
            return actualStringValue.compareTo(stringValue) < 0;
          case LTE:
            return actualStringValue.compareTo(stringValue) <= 0;
          case NEQ:
            return !actualStringValue.equals(stringValue);
          default:
            throw new RuntimeException("Unexpected operator. This should never happen!");
          }
        }
      case FALSE:
        return false;
      case TRUE:
        return true;
      case NOTNULL:
        return isNotNull(metadata, data);
      case NULL:
        return !isNotNull(metadata, data);
      default:
        throw new RuntimeException("Unexpected condition type " + conditionType);
    }
  }
  
  private boolean isNotNull(Metadata metadata, Map<String, String> data) {
    // Get the field that is referenced in this condition expression
    Field field = metadata.getField(fieldId);
    FieldType ft = field.getFieldType();
    
    // Deal with special case where field is a checkbox - in this case we need to check that any
    // of the possible values are populated
    if (ft.equals(FieldType.CHECKBOX)) {
      for (Field f : metadata.getFields()) {
        // The checkbox entry fields start with the name of the checkbox field
        if (f.getFieldId().startsWith(this.fieldId) && !f.getFieldId().equals(this.fieldId)) {
          String value = data.get(f.getFieldId());
          if (value != null && !value.equals("0")) {
            return true;
          }
        }
      }
      return false;
    } else {
      String value = data.get(fieldId);
      return value != null && !value.isEmpty();
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
  public boolean referencesData() {
    if (conditionType.equals(ConditionType.TRUE) || conditionType.equals(ConditionType.FALSE)) {
      return false;
    }
    return true;
  }

}
