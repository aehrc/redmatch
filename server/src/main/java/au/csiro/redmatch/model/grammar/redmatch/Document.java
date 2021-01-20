/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.model.grammar.redmatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.csiro.redmatch.model.Field;
import au.csiro.redmatch.model.Metadata;
import au.csiro.redmatch.model.Field.TextValidationType;
import au.csiro.redmatch.model.grammar.GrammarObject;

/**
 * Represents a rules document.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class Document extends GrammarObject {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(Document.class);
  
  private final List<Rule> rules = new ArrayList<>();

  public Document() {

  }

  /**
   * Returns all the rules.
   * 
   * @return A list of all rules.
   */
  public List<Rule> getRules() {
    return rules;
  }

  /**
   * Returns a map with the REDCap field ids referenced in the rules as the key and a boolean value
   * indicating if the field needs to be mapped. A field requires mapping when it is part of the
   * "resource" section of the rule and uses the CONCEPT, CONCEPT_SELECTED or CODE_SELECTED keyword.
   * The CONCEPT keyword only requires a mapping when used on a REDCap field of type TEXT that is 
   * not an autocomplete field.
   * 
   * @param metadata The metadata of the project. Required to calculate which fields are required
   * based on the rules.
   * 
   * @return A map with the REDCap field ids referenced in the rules and a boolean value that 
   *     indicates if the field needs to be mapped
   */
  public Map<String, Boolean> getReferencedFields(Metadata metadata) {
    final Map<String, Boolean> res = new HashMap<>();

    for (Rule rule : rules) {
      for(Condition c : rule.getConditions()) {
        final LinkedList<Condition> toProcess = new LinkedList<>();
        toProcess.add(c);
  
        while (!toProcess.isEmpty()) {
          final Condition cond = toProcess.removeFirst();
          if (cond instanceof ConditionNode) {
            ConditionNode cn = (ConditionNode) cond;
            toProcess.add(cn.getLeftCondition());
            toProcess.add(cn.getRightCondition());
          } else if (cond instanceof ConditionExpression) {
            ConditionExpression ce = (ConditionExpression) cond;
            String fieldId = ce.getFieldId();
            if (fieldId != null && !res.containsKey(fieldId)) {
              res.put(fieldId, Boolean.FALSE);
            }
            final List<String> resourceIds = ce.getResourceIds();
            if (resourceIds != null) {
              for (String resourceId : resourceIds) {
                if (!res.containsKey(resourceId)) {
                  res.put(resourceId, false);
                }
              }
            }
          } else {
            throw new RuntimeException("Unexpected Condition type " + cond.getClass().getName());
          }
        }
      }

      for (Resource resource : rule.getResources()) {
        for (AttributeValue av : resource.getResourceAttributeValues()) {
          final Value val = av.getValue();
          if (val != null && val instanceof FieldBasedValue) {
            FieldBasedValue fbv = (FieldBasedValue) val;
            String rulesFieldId = fbv.getFieldId();
            if (fbv instanceof ConceptSelectedValue || fbv instanceof CodeSelectedValue) {
              for (Field field : metadata.getFields()) {
                // Need to do this to account for xx___yy type fields
                if (field.getFieldId().startsWith(rulesFieldId) 
                    && !field.getFieldId().equals(rulesFieldId)) {
                  res.put(field.getFieldId(), Boolean.TRUE);
                }
              }
            } else if (fbv instanceof ConceptValue) {
              Field f = metadata.getField(rulesFieldId);
              
              if (f == null) {
                log.warn("Found null field " + rulesFieldId + " in expression " + fbv);
                continue;
              }
              
              switch (f.getFieldType()) {
              case YESNO:
              case DROPDOWN:
              case RADIO:
              case DROPDOW_OR_RADIO_OPTION:
              case CHECKBOX:
              case CHECKBOX_OPTION:
              case TRUEFALSE:
                res.put(rulesFieldId, Boolean.TRUE);
                break;
              case TEXT:
                if (!TextValidationType.FHIR_TERMINOLOGY.equals(f.getTextValidationType())) {
                  res.put(rulesFieldId, Boolean.TRUE);
                }
                break;
              default:
                res.put(rulesFieldId, Boolean.FALSE);
                break;
              }
            } else {
              res.put(rulesFieldId, Boolean.FALSE);
            } 
          }
        }
      }
    }

    return res;
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
  public boolean referencesData() {
    boolean referencesData = false;
    for (Rule r : rules) {
      referencesData = referencesData || r.referencesData();
    }
    return referencesData;
  }

}
