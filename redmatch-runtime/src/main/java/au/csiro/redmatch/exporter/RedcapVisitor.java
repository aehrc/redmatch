/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.exporter;

import au.csiro.redmatch.compiler.*;
import au.csiro.redmatch.compiler.ConditionNode.ConditionNodeOperator;
import au.csiro.redmatch.compiler.Schema;
import au.csiro.redmatch.model.*;
import au.csiro.redmatch.terminology.TerminologyService;
import au.csiro.redmatch.util.Progress;
import au.csiro.redmatch.util.ProgressReporter;
import au.csiro.redmatch.util.ReflectionUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.hl7.fhir.r4.model.DomainResource;
import org.jgrapht.Graph;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Visitor implementation for a REDCap data source. Visits {@link Rule}s and retrieves the resources that need to be
 * created for a {@link Row} of data.
 *
 * @author Alejandro Metke Jimenez
 */
public class RedcapVisitor extends BaseVisitor implements GrammarObjectVisitor {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedcapVisitor.class);

  private final au.csiro.redmatch.model.Schema schema;
  private final List<PatientData> patientData = new ArrayList<>();
  private final Map<String, DomainResource> fhirResourceMap = new HashMap<>();
  private final VersionedFhirPackage defaultFhirPackage;
  private final ProgressReporter progressReporter;
  private final CancelChecker cancelToken;

  public RedcapVisitor(Document doc, Set<String> uniqueIds, HapiReflectionHelper hapiReflectionHelper,
                     TerminologyService terminologyService, VersionedFhirPackage defaultFhirPackage, List<Row> rows,
                       ProgressReporter progressReporter, CancelChecker cancelToken) {
    super(doc, uniqueIds, hapiReflectionHelper, terminologyService);
    this.defaultFhirPackage = defaultFhirPackage;
    this.schema = doc.getSchema();
    this.progressReporter = progressReporter;
    this.cancelToken = cancelToken;

    // Populate patient data from rows - this will be different for other sources
    if (progressReporter != null) {
      progressReporter.reportProgress(Progress.reportStart("Populating patient data"));
    }
    final String uniqueField = doc.getSchema().getUniqueFieldId();

    int totalRows = rows.size();
    double div = totalRows / 100.0;

    for (int i = 0; i < rows.size(); i++) {
      Row row = rows.get(i);
      Graph<JsonElement, LabeledEdge> data = row.getData();

      // Find recordId
      JsonObject patientVertex = findPatientVertex(data);
      assert patientVertex != null;
      String recordId = patientVertex.get(uniqueField).getAsString();
      PatientData patientDatum = new PatientData(recordId);

      for (JsonElement jsonElement : data.vertexSet()) {
        if (jsonElement.isJsonObject()) {
          JsonObject jsonObject = jsonElement.getAsJsonObject();
          patientDatum.addObject(jsonObject);
        }
      }
      patientData.add(patientDatum);

      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportProgress((int) Math.floor(i / div)));
      }
      if(cancelToken != null && cancelToken.isCanceled()) {
        throw new TransformationException("Transformation canceled!");
      }
    }
    if (progressReporter != null) {
      progressReporter.reportProgress(Progress.reportEnd());
    }

    visit(doc);
  }

  public Map<String, DomainResource> getFhirResourceMap() {
    return fhirResourceMap;
  }

  @Override
  public void visit(Document document) {
    au.csiro.redmatch.model.Schema.SchemaType schemaType = document.getSchema().getSchemaType();
    if (schemaType.equals(au.csiro.redmatch.model.Schema.SchemaType.REDCAP)) {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportStart("Evaluating rules"));
      }
      int totalRows = document.getRules().size();
      double div = totalRows / 100.0;
      int i = 1;
      for (Rule rule : document.getRules()) {
        log.debug("Evaluating rule in document " + rule.toStringShort());
        visit(rule, patientData);
        if (progressReporter != null) {
          progressReporter.reportProgress(Progress.reportProgress((int) Math.floor(i++ / div)));
        }
        if(cancelToken != null && cancelToken.isCanceled()) {
          return;
        }
      }
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportEnd());
      }
    } else {
      throw new TransformationException("Expected a REDCap schema but got " + schemaType);
    }
  }

  private List<PatientData> filter(List<PatientData> patientData, Condition condition) {
    List<PatientData> res = new ArrayList<>();
    for (PatientData patientDatum : patientData) {
      boolean matchesAny = false;
      for (JsonObject object : patientDatum.getObjects()) {
        if (evaluate(condition, object)) {
          matchesAny = true;
          break;
        }
      }
      if (matchesAny) {
        res.add(patientDatum);
      }
    }

    return res;
  }

  public void visit(Rule rule, List<PatientData> patientData) {
    log.debug("Evaluating rule " + rule.toStringShort());
    Condition condition = rule.getCondition();

    Body body = rule.getBody();
    List<PatientData> bodyData = filter(patientData, condition);
    visit(body, bodyData);

    List<PatientData> elseData = new ArrayList<>(patientData);
    elseData.removeAll(bodyData);
    Body elseBody = rule.getElseBody();
    if (elseBody != null && !patientData.isEmpty()) {
      visit(elseBody, elseData);
    }
  }

  private boolean evaluate(Condition c, JsonObject data) {
    if (c instanceof  ConditionExpression) {
      ConditionExpression ce = (ConditionExpression) c;
      if (ce.isNegated()) {
        return !doEvaluate(ce, data);
      } else {
        return doEvaluate(ce, data);
      }
    } else if (c instanceof  ConditionNode) {
      ConditionNode cn = (ConditionNode) c;
      ConditionNodeOperator op = cn.getOp();
      Condition leftCondition = cn.getLeftCondition();
      Condition rightCondition = cn.getRightCondition();
      switch (op) {
        case AND:
          return evaluate(leftCondition, data) && evaluate(rightCondition, data);
        case OR:
          return evaluate(leftCondition, data) || evaluate(rightCondition, data);
        default:
          throw new RuntimeException("Unexpected condition node operator. This should never happen!");
      }
    } else {
      throw new RuntimeException("Unexpected Condition: " + c + ". This should not happen!");
    }
  }

  private boolean doEvaluate(ConditionExpression ce, JsonObject data) {
    String fieldId = ce.getFieldId();
    Integer intValue = ce.getIntValue();
    Double numericValue = ce.getNumericValue();
    String stringValue = ce.getStringValue();
    ConditionExpression.ConditionExpressionOperator operator = ce.getOperator();

    switch (ce.getConditionType()) {
      case EXPRESSION:
        if (intValue == null && numericValue == null && stringValue == null) {
          throw new TransformationException("No value has been specified for this expression. [" + this + "]");
        }

        String actualStringValue = null;
        if (!data.has(fieldId)) {
          // See if this is an option and extract the value from the name
          if (fieldId.contains("___")) {
            String[] parts = fieldId.split("___");
            if (data.has(parts[0])) {
              String chosenVal = data.get(parts[0]).getAsString();
              if (chosenVal != null && chosenVal.equals(parts[1])) {
                actualStringValue = "1";
              } else {
                actualStringValue = "0";
              }
            }
          }
        } else {
          actualStringValue = data.get(fieldId).getAsString();
        }

        if (actualStringValue == null || actualStringValue.isEmpty()) {
          return false;
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
                return fieldValue == numericValue;
              case GT:
                return fieldValue > numericValue;
              case GTE:
                return fieldValue >= numericValue;
              case LT:
                return fieldValue < numericValue;
              case LTE:
                return fieldValue <= numericValue;
              case NEQ:
                return fieldValue != numericValue;
              default:
                throw new RuntimeException("Unexpected operator. This should never happen!");
            }
          } catch (NumberFormatException e) {
            throw new TransformationException("Could not parse value of field " + fieldId
              + " into a number (" + data.get(fieldId) + ") [" + this + "]");
          }
        } else if (intValue != null) {
          try {
            final int fieldValue = Integer.parseInt(actualStringValue);
            switch (operator) {
              case EQ:
                return fieldValue == intValue;
              case GT:
                return fieldValue > intValue;
              case GTE:
                return fieldValue >= intValue;
              case LT:
                return fieldValue < intValue;
              case LTE:
                return fieldValue <= intValue;
              case NEQ:
                return fieldValue != intValue;
              default:
                throw new RuntimeException("Unexpected operator. This should never happen!");
            }
          } catch (NumberFormatException e) {
            throw new TransformationException("Could not parse value of field " + fieldId + " into an integer (" +
              data.get(fieldId) + ") [" + this + "]");
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
        return isNotNull(fieldId, data);
      case NULL:
        return !isNotNull(fieldId, data);
      default:
        throw new RuntimeException("Unexpected condition type " + ce.getConditionType());
    }
  }

  private boolean isNotNull(String fieldId, JsonObject data) {
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
            if (data.has(f.getFieldId()) && !"0".equals(data.get(f.getFieldId()).getAsString())) {
              return true;
            }
          }
        }
        return false;
      } else {
        return (data.has(fieldId) && !data.get(fieldId).getAsString().isEmpty());
      }
    } else {
      throw new UnsupportedOperationException("Only REDCap is supported at the moment.");
    }
  }

  private VersionedFhirPackage getFhirPackage(Document doc) {
    VersionedFhirPackage fhirPackage = doc.getFhirPackage();
    if (fhirPackage == null) {
      fhirPackage = defaultFhirPackage;
    }
    return fhirPackage;
  }


  /**
   * Visits the body of a rule. Patient data is only the subset that matches the condition.
   *
   * @param body The body of a rule.
   * @param patientData The applicable patient data.
   */
  private void visit(Body body, List<PatientData> patientData) {

    if (requiresData(body)) {
      for (PatientData patientDatum : patientData) {
        String recordId = patientDatum.getId();
        for (JsonObject object : patientDatum.getObjects()) {
          // Return any resources directly in the rule
          for (Resource resource : body.getResources()) {
            DomainResource domainResource = createResource(resource, object, recordId, getFhirPackage(doc));
            String id = domainResource.getId();
            if (fhirResourceMap.containsKey(id)) {
              // Merge with existing resource if already exists
              DomainResource existingResource = fhirResourceMap.get(id);
              //existingResource.copyValues(domainResource);
              try {
                ReflectionUtils.merge(domainResource, existingResource);
              } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new TransformationException("There was a problem merging the generated FHIR resources.", e);
              }
            } else {
              fhirResourceMap.put(id, domainResource);
            }
          }
        }
      }
    } else {
      for (Resource resource : body.getResources()) {
        DomainResource domainResource = createResource(resource, null, null, getFhirPackage(doc));
        String id = domainResource.getId();
        if (fhirResourceMap.containsKey(id)) {
          // Merge with existing resource if already exists
          DomainResource existingResource = fhirResourceMap.get(id);
          //existingResource.copyValues(domainResource);
          try {
            ReflectionUtils.merge(domainResource, existingResource);
          } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new TransformationException("There was a problem merging the generated FHIR resources.", e);
          }
        }
        fhirResourceMap.put(id, domainResource);
      }
    }

    // Recursively evaluate any nested rules
    for (Rule rule : body.getRules()) {
      log.debug("Evaluating rule in body " + rule.toStringShort());
      visit(rule, patientData);
    }
  }

  @Override
  public void visit(Attribute a) {

  }

  @Override
  public void visit(AttributeValue a) {

  }

  @Override
  public void visit(Body b) {

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
  public void visit(Rule r) {

  }

  @Override
  public void visit(RuleList r) {

  }

  @Override
  public void visit(Schema s) {

  }

  private boolean requiresData(Body body) {
    for (Resource resource : body.getResources()) {
      String resourceId = resource.getResourceType() + "<" + resource.getResourceId() + ">";
      if (!uniqueIds.contains(resourceId)) {
        return true;
      }
    }
    return false;
  }

  private JsonObject findPatientVertex(Graph<JsonElement, LabeledEdge> data) {
    for (JsonElement jsonElement : data.vertexSet()) {
      if (jsonElement.isJsonObject()) {
        JsonObject vertex = jsonElement.getAsJsonObject();
        String vertexType = vertex.get(LabeledDirectedMultigraph.VERTEX_TYPE_FIELD).getAsString();
        if (vertexType.equals("Patient")) {
          return vertex;
        }
      }
    }
    return null;
  }
}
