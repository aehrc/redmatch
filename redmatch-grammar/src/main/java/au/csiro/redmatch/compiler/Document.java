/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.compiler;

import au.csiro.redmatch.model.Schema;
import au.csiro.redmatch.model.VersionedFhirPackage;
import org.eclipse.lsp4j.Diagnostic;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a rules document.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class Document extends GrammarObject {

  /**
   * Pattern to remove options suffix.
   */
  private static final Pattern p = Pattern.compile("(.*)(___[0-9]+)");

  /**
   * List of transformation rules.
   */
  private final List<Rule> rules = new ArrayList<>();

  /**
   * The schema of the data source.
   */
  private Schema schema;

  /**
   * The target FHIR package. Can be null and in that case a default FHIR package will be used.
   */
  private VersionedFhirPackage fhirPackage;

  /**
   * The server definition that represents the connection details to use to get data for this document. This name should
   * match the name of a server definition in the redmatch-config.yaml file.
   */
  private String server;

  /**
   * A map of {@link Mapping}s, indexed by field id.
   */
  private final Map<String, Mapping> mappings = new HashMap<>();

  /**
   * A list of compilation issues.
   */
  private List<Diagnostic> diagnostics;

  /**
   * Returns all the rules.
   * 
   * @return A list of all rules.
   */
  public List<Rule> getRules() {
    return rules;
  }

  /**
   * Returns the schema of the data source.
   *
   * @return The schema.
   */
  public Schema getSchema() {
    return schema;
  }

  /**
   * Returns the server definition that represents the connection details to use to get data for this document. This
   * name should match the name of a server definition in the redmatch-config.yaml file.
   *
   * @return The server definition name.
   */
  public String getServer() { return server; }

  /**
   * Returns the list of compilation problems.
   *
   * @return The list of compilation problems.
   */
  public List<Diagnostic> getDiagnostics() {
    return diagnostics;
  }

  /**
   * Returns the mappings.
   *
   * @return The mappings.
   */
  public Map<String, Mapping> getMappings() {
    return mappings;
  }

  /**
   * Returns a map with the field ids referenced in the rules.
   *
   * @param excludeOptions If true, the field names that represent options, e.g. mito_ha_type___1, are excluded.
   * @return A set with the field ids referenced in the rules.
   */
  public Set<String> getReferencedFields(boolean excludeOptions) {
    final Set<String> res = new HashSet<>();

    for (Rule rule : rules) {
      for (ConditionExpression ce :rule.getConditionExpressions()) {
        String fieldId = ce.getFieldId();
        if (fieldId != null) {
          if(excludeOptions) {
            fieldId = removeOption(fieldId);
          }
          res.add(fieldId);
        }
      }
      for (Resource resource : rule.getResources()) {
        for (AttributeValue av : resource.getResourceAttributeValues()) {
          final Value val = av.getValue();

          if (val instanceof FieldBasedValue) {
            FieldBasedValue fbv = (FieldBasedValue) val;
            String fieldId = fbv.getFieldId();
            if(excludeOptions) {
              fieldId = removeOption(fieldId);
            }
            res.add(fieldId);
          }
        }
      }
    }

    // If this is a REDCap schema them we add the unique field, which is the first field in the schema
    if (schema.getSchemaType().equals(Schema.SchemaType.REDCAP)) {
      String uniqueField = schema.getFields().get(0).getFieldId();
      if(excludeOptions) {
        uniqueField = removeOption(uniqueField);
      }
      res.add(uniqueField);
    }

    return res;
  }

  public VersionedFhirPackage getFhirPackage() {
    return fhirPackage;
  }

  private String removeOption(String s) {
    Matcher m = p.matcher(s);
    if (m.find()) {
      return m.replaceFirst("$1");
    } else {
      return s;
    }
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
  }

  public void setServer(String server) { this.server = server; }

  public void setDiagnostics(List<Diagnostic> diagnostics) {
    this.diagnostics = diagnostics;
  }

  public void setFhirPackage(VersionedFhirPackage fhirPackage) {
    this.fhirPackage = fhirPackage;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Rule rule : rules) {
      sb.append(rule.toString());
      sb.append("\n");
    }
    return sb.toString();
  }

  @Override
  public DataReference referencesData() {
    DataReference referencesData = DataReference.NO;
    
    for(Rule rule : rules) {
      switch(rule.referencesData()) {
        case YES:
          referencesData = DataReference.YES;
          break;
        case RESOURCE:
          if (referencesData.equals(DataReference.NO)) {
            referencesData = DataReference.RESOURCE;
          }
          break;
        case NO:
          // Do nothing
          break;
        
        default:
          throw new RuntimeException("Unexpected value " + referencesData);
      }
    }
    return referencesData;
  }

  @Override
  public void accept(GrammarObjectVisitor v) {
    v.visit(this);
  }

}
