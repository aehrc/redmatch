package au.csiro.redmatch.exporter;

import au.csiro.redmatch.compiler.*;
import au.csiro.redmatch.compiler.ConditionNode.ConditionNodeOperator;
import au.csiro.redmatch.model.Field;
import au.csiro.redmatch.model.RedcapField;
import au.csiro.redmatch.model.Row;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Visitor implementation for a REDCap data source. Visits {@link Rule}s and retrieves the resources that need to be
 * created for a {@link Row} of data.
 *
 * @author Alejandro Metke Jimenez
 */
public class RedcapVisitor implements GrammarObjectVisitor {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedcapVisitor.class);

  private final au.csiro.redmatch.model.Schema schema;
  private final Row data;
  private final List<Resource> resources = new ArrayList<>();

  public RedcapVisitor(au.csiro.redmatch.model.Schema schema, Row data) {
    this.schema = schema;
    this.data = data;
  }

  public List<Resource> getResources() {
    return resources;
  }

  @Override
  public void visit(Document d) {
    au.csiro.redmatch.model.Schema.SchemaType schemaType = d.getSchema().getSchemaType();
    if (schemaType.equals(au.csiro.redmatch.model.Schema.SchemaType.REDCAP)) {
      for (Rule rule : d.getRules()) {
        visit(rule);
      }
    } else {
      throw new TransformationException("Expected a REDCap schema but got " + schemaType);
    }
  }

  @Override
  public void visit(Rule r) {
    Condition c = r.getCondition();
    Body body = r.getBody();
    Body elseBody = r.getElseBody();

    if (evaluate(c)) {
      // TODO: add resources from body - look at Rule in previous implementation
      visit(body);
    } else if (elseBody != null) {
      // TODO: add resources from elseBody
      visit(elseBody);
    }
  }

  private boolean evaluate(Condition c) {
    if (c instanceof  ConditionExpression) {
      ConditionExpression ce = (ConditionExpression) c;
      if (ce.isNegated()) {
        return !doEvaluate(ce);
      } else {
        return doEvaluate(ce);
      }
    } else if (c instanceof  ConditionNode) {
      ConditionNode cn = (ConditionNode) c;
      ConditionNodeOperator op = cn.getOp();
      Condition leftCondition = cn.getLeftCondition();
      Condition rightCondition = cn.getRightCondition();
      switch (op) {
        case AND:
          return evaluate(leftCondition) && evaluate(rightCondition);
        case OR:
          return evaluate(leftCondition) || evaluate(rightCondition);
        default:
          throw new RuntimeException("Unexpected condition node operator. This should never happen!");
      }
    } else {
      throw new RuntimeException("Unexpected Condition: " + c + ". This should not happen!");
    }
  }

  private boolean doEvaluate(ConditionExpression ce) {
    Map<String, String> data;
    if (this.data != null) {
      data = this.data.getData();
    } else {
      data = Collections.emptyMap();
    }
    String fieldId = ce.getFieldId();
    Integer intValue = ce.getIntValue();
    Double numericValue = ce.getNumericValue();
    String stringValue = ce.getStringValue();
    ConditionExpression.ConditionExpressionOperator operator = ce.getOperator();

    switch (ce.getConditionType()) {
      case EXPRESSION:
        if (intValue == null && numericValue == null && stringValue == null) {
          throw new TransformationException("No value has been specified for this expression. ["
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

        // Get data type from schema
        Field field = schema.getField(fieldId);
        if (field == null) {
          throw new TransformationException("No field " + fieldId + " found in the schema.");
        }

        if (numericValue != null) {
          try {
            final double fieldValue = Double.parseDouble(actualStringValue);
            switch (operator) {
              case EQ:
                return (double) fieldValue == numericValue;
              case GT:
                return (double) fieldValue > numericValue;
              case GTE:
                return (double) fieldValue >= numericValue;
              case LT:
                return (double) fieldValue < numericValue;
              case LTE:
                return (double) fieldValue <= numericValue;
              case NEQ:
                return (double) fieldValue != numericValue;
              default:
                throw new RuntimeException("Unexpected operator. This should never happen!");
            }
          } catch (NumberFormatException e) {
            throw new TransformationException("Could not parse value of field " + fieldId
              + " into a number (" + data.get(fieldId) + ") [" + this.toString() + "]");
          }
        } else if (intValue != null) {
          try {
            final int fieldValue = Integer.parseInt(actualStringValue);
            switch (operator) {
              case EQ:
                return (int) fieldValue == intValue;
              case GT:
                return (int) fieldValue > intValue;
              case GTE:
                return (int) fieldValue >= intValue;
              case LT:
                return (int) fieldValue < intValue;
              case LTE:
                return (int) fieldValue <= intValue;
              case NEQ:
                return (int) fieldValue != intValue;
              default:
                throw new RuntimeException("Unexpected operator. This should never happen!");
            }
          } catch (NumberFormatException e) {
            throw new TransformationException("Could not parse value of field " + fieldId
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
        return isNotNull(fieldId);
      case NULL:
        return !isNotNull(fieldId);
      default:
        throw new RuntimeException("Unexpected condition type " + ce.getConditionType());
    }
  }

  private boolean isNotNull(String fieldId) {
    // Get the field that is referenced in this condition expression
    Field field = schema.getField(fieldId);
    if (field instanceof RedcapField) {
      RedcapField rfield = (RedcapField) field;
      RedcapField.FieldType ft = rfield.getFieldType();

      // Deal with special case where field is a checkbox - in this case we need to check that any
      // of the possible values are populated
      if (ft.equals(RedcapField.FieldType.CHECKBOX)) {
        for (Field f : schema.getFields()) {
          // The checkbox entry fields start with the name of the checkbox field
          if (f.getFieldId().startsWith(fieldId) && !f.getFieldId().equals(fieldId)) {
            String value = data.getData().get(f.getFieldId());
            if (value != null && !value.equals("0")) {
              return true;
            }
          }
        }
        return false;
      } else {
        String value = data.getData().get(fieldId);
        return value != null && !value.isEmpty();
      }
    } else {
      throw new UnsupportedOperationException("Only REDCap is supported at the moment.");
    }
  }

  @Override
  public void visit(Body b) {
    // Return any resources directly in the rule
    this.resources.addAll(b.getResources());

    // Recursively evaluate any nested rules
    for (Rule r : b.getRules()) {
      visit(r);
    }
  }

  @Override
  public void visit(Attribute a) {

  }

  @Override
  public void visit(AttributeValue a) {

  }

  @Override
  public void visit(Value v) {

  }

  @Override
  public void visit(Condition c) {

  }

  @Override
  public void visit(Mapping m) {

  }

  @Override
  public void visit(RepeatsClause r) {

  }

  @Override
  public void visit(Resource r) {

  }

  @Override
  public void visit(RuleList r) {

  }

  @Override
  public void visit(Schema s) {

  }
}
