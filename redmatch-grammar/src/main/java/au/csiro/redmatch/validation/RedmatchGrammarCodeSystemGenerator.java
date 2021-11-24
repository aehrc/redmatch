/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.validation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemContentMode;
import org.hl7.fhir.r4.model.CodeSystem.CodeSystemHierarchyMeaning;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.FilterOperator;
import org.hl7.fhir.r4.model.CodeSystem.PropertyType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.ElementDefinition.TypeRefComponent;
import org.hl7.fhir.r4.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.StructureDefinition;

import ca.uhn.fhir.context.FhirVersionEnum;

/**
 * Creates a code system from the FHIR metadata that can be used for validation and searching.
 * 
 * @author Alejandro Metke-Jimenez
 *
 */
public class RedmatchGrammarCodeSystemGenerator {

  private static final Log log = LogFactory.getLog(RedmatchGrammarCodeSystemGenerator.class);

  private FhirVersionEnum fhirVersion = FhirVersionEnum.R4;
  private final Map<String, StructureDefinition> resourceMap = new HashMap<>();
  private final Map<String, StructureDefinition> primitiveTypeMap = new HashMap<>();
  private final Map<String, StructureDefinition> complexTypeMap = new HashMap<>();
  private final Map<String, String> pathToTypeMap = new HashMap<>();
  private final List<ConceptDefinitionComponentElementDefinitionPair> missingTypes = new ArrayList<>();

  private final Set<String> allCodes = new HashSet<>();
  private final Set<String> allParents = new HashSet<>();

  public RedmatchGrammarCodeSystemGenerator() {

  }

  public CodeSystem[] createCodeSystems(Bundle types, Bundle resources) {
    log.info("Indexing structure definitions");
    for (BundleEntryComponent bec : types.getEntry()) {
      org.hl7.fhir.r4.model.Resource res = bec.getResource();
      if (res.getResourceType().equals(ResourceType.StructureDefinition)
        && !((StructureDefinition) res).getAbstract()) {
        StructureDefinition sd = (StructureDefinition) res;
        switch (sd.getKind()) {
          case COMPLEXTYPE:
            complexTypeMap.put(sd.getName(), sd);
            break;
          case PRIMITIVETYPE:
            primitiveTypeMap.put(sd.getName(), sd);
            break;
          case LOGICAL:
          case NULL:
          case RESOURCE:
          default:
            log.debug("Ignoring " + sd.getName());
            break;
        }
      }
    }

    for (BundleEntryComponent bec : resources.getEntry()) {
      org.hl7.fhir.r4.model.Resource res = bec.getResource();
      if (res.getResourceType().equals(ResourceType.StructureDefinition)
        && !((StructureDefinition) res).getAbstract()) {
        resourceMap.put(((StructureDefinition) res).getName(), (StructureDefinition) res);
      }
    }

    // Create resource code system
    log.info("Creating resources code system");
    CodeSystem resCs = createBaseResourceCodeSystem();

    for (String key : resourceMap.keySet()) {
      StructureDefinition res = resourceMap.get(key);
      processStructureDefinition(resCs, res, false, "", true);
    }

    allCodes.add("DomainResource");
    addMissingTypes();

    allParents.removeAll(allCodes);
    for (String code : allParents) {
      log.warn("Parent code " + code + " does not exist.");
    }

    allCodes.clear();
    allParents.clear();
    log.info("Creating complex types code system");
    CodeSystem ctCs = createBaseComplexTypesCodeSystem();

    for (String key : complexTypeMap.keySet()) {
      StructureDefinition ct = complexTypeMap.get(key);
      log.info("Processing " + ct.getName());
      processStructureDefinition(ctCs, ct, false, "", false);
    }
    allCodes.add("Element");
    addMissingTypes();

    allParents.removeAll(allCodes);
    for (String code : allParents) {
      log.warn("Parent code " + code + " does not exist.");
    }

    return new CodeSystem[] { resCs, ctCs };
  }

  private void addMissingTypes() {
    // Add the types for missing concepts
    for (ConceptDefinitionComponentElementDefinitionPair cdcEd : missingTypes) {
      ElementDefinition ed = cdcEd.getEd();
      ConceptDefinitionComponent cdc = cdcEd.getCdc();
      if (ed.hasContentReference()) {
        String ref = ed.getContentReference();
        if (ref.startsWith("#")) {
          ref = ref.substring(1);
        }

        String type = this.pathToTypeMap.get(ref);
        if (type == null) {
          throw new RuntimeException("Could not find referenced type " + ref
            + ". This should never happen!");
        }
        cdc.addProperty().setCode("type").setValue(new StringType(type));
      } else {
        // This is a resource path

        String code = cdc.getCode();
        if (code.contains(".")) {
          throw new RuntimeException("Code " + code
            + " contains a dot. This should never happen!");
        }
        // TODO: we might want to use the StructureDefinition instead
        cdc.addProperty().setCode("type").setValue(new StringType(cdc.getCode()));
      }
    }
    missingTypes.clear();
  }

  private CodeSystem createBaseResourceCodeSystem() {
    CodeSystem cs = new CodeSystem();
    cs.setId("redmatch-fhir-resources");
    cs.setUrl("http://csiro.au/redmatch-fhir/resources");
    cs.setVersion("1.0");
    cs.setName("Redmatch Grammar for FHIR Resources " + fhirVersion.getFhirVersionString());
    cs.setStatus(PublicationStatus.ACTIVE);
    cs.setDescription("A code system with all the valid paths used to refer to attributes of "
      + "resources in FHIR version " + fhirVersion.getFhirVersionString());
    cs.setValueSet("http://csiro.au/redmatch-fhir/resources?vs");
    cs.setHierarchyMeaning(CodeSystemHierarchyMeaning.ISA);
    cs.setContent(CodeSystemContentMode.COMPLETE);
    cs.setExperimental(false);
    cs.setCompositional(false);
    cs.setVersionNeeded(false);
    cs.addProperty()
      .setCode("parent")
      .setDescription("Parent codes.")
      .setType(PropertyType.CODE);
    cs.addProperty()
      .setCode("root")
      .setDescription("Indicates if this concept is a root concept (i.e. Thing is equivalent or "
        + "a direct parent)")
      .setType(PropertyType.BOOLEAN);
    cs.addProperty()
      .setCode("deprecated")
      .setDescription("Indicates if this concept is deprecated.")
      .setType(PropertyType.BOOLEAN);
    cs.addProperty()
      .setCode("min")
      .setDescription("Minimun cardinality")
      .setType(PropertyType.INTEGER);
    cs.addProperty()
      .setCode("max")
      .setDescription("Maximum cardinality")
      .setType(PropertyType.STRING);
    cs.addProperty()
      .setCode("type")
      .setDescription("Data type for this element.")
      .setType(PropertyType.STRING);
    cs.addProperty()
      .setCode("targetProfile")
      .setDescription("If this code represents a Reference attribute, this property contains an "
        + "allowed target profile.")
      .setType(PropertyType.STRING);
    cs.addFilter()
      .setCode("root")
      .setValue("True or false.")
      .addOperator(FilterOperator.EQUAL);
    cs.addFilter()
      .setCode("deprecated")
      .setValue("True or false.")
      .addOperator(FilterOperator.EQUAL);

    // Create root concept
    ConceptDefinitionComponent root = cs.addConcept()
      .setCode("DomainResource")
      .setDisplay("DomainResource");
    root.addProperty().setCode("root").setValue(new BooleanType(true));
    root.addProperty().setCode("deprecated").setValue(new BooleanType(false));

    return cs;
  }

  private CodeSystem createBaseComplexTypesCodeSystem() {
    CodeSystem cs = new CodeSystem();
    cs.setId("redmatch-fhir-complex-types");
    cs.setUrl("http://csiro.au/redmatch-fhir/complex-types");
    cs.setVersion("1.0");
    cs.setName("Redmatch Grammar for FHIR Complex Types "
      + this.fhirVersion.getFhirVersionString());
    cs.setStatus(PublicationStatus.ACTIVE);
    cs.setDescription("A code system with all the valid paths used to refer to attributes of "
      + "complex types in FHIR version " + this.fhirVersion.getFhirVersionString());
    cs.setValueSet("http://csiro.au/redmatch-fhir/complex-types?vs");
    cs.setHierarchyMeaning(CodeSystemHierarchyMeaning.ISA);
    cs.setContent(CodeSystemContentMode.COMPLETE);
    cs.setExperimental(false);
    cs.setCompositional(false);
    cs.setVersionNeeded(false);
    cs.addProperty()
      .setCode("parent")
      .setDescription("Parent codes.")
      .setType(PropertyType.CODE);
    cs.addProperty()
      .setCode("root")
      .setDescription("Indicates if this concept is a root concept (i.e. Thing is equivalent or "
        + "a direct parent)")
      .setType(PropertyType.BOOLEAN);
    cs.addProperty()
      .setCode("deprecated")
      .setDescription("Indicates if this concept is deprecated.")
      .setType(PropertyType.BOOLEAN);
    cs.addProperty()
      .setCode("min")
      .setDescription("Minimun cardinality")
      .setType(PropertyType.INTEGER);
    cs.addProperty()
      .setCode("max")
      .setDescription("Maximum cardinality")
      .setType(PropertyType.STRING);
    cs.addProperty()
      .setCode("type")
      .setDescription("Data type for this element.")
      .setType(PropertyType.STRING);
    cs.addProperty()
      .setCode("targetProfile")
      .setDescription("If this code represents a Reference attribute, this property contains an "
        + "allowed target profile.")
      .setType(PropertyType.STRING);
    cs.addFilter()
      .setCode("root")
      .setValue("True or false.")
      .addOperator(FilterOperator.EQUAL);
    cs.addFilter()
      .setCode("deprecated")
      .setValue("True or false.")
      .addOperator(FilterOperator.EQUAL);

    // Create root concept
    ConceptDefinitionComponent root = cs.addConcept()
      .setCode("Element")
      .setDisplay("Element");
    root.addProperty().setCode("root").setValue(new BooleanType(true));
    root.addProperty().setCode("deprecated").setValue(new BooleanType(false));

    return cs;
  }

  private boolean isValueX(ElementDefinition ed) {
    return ed.hasType() && ed.getType().size() > 1;
  }

  /**
   * Creates codes for all the valid paths of a structure definition.
   *
   * @param cs The code system.
   * @param sd The structure definition.
   * @param nested True if this is being processed as an attribute of another structure definition.
   * @param prefix If nested is true, then this contains the base path for all the paths in this
   * structure definition.
   */
  private void processStructureDefinition(CodeSystem cs, StructureDefinition sd, boolean nested,
                                          String prefix, boolean isResource) {

    if (sd.getName().equals("Extension")) {
      return;
    }

    // Prevent creating duplicate codes when complex types are based on others, e.g. MoneyQuantity
    // and Quantity
    String realType = sd.getSnapshot().getElementFirstRep().getId();
    if (allCodes.contains(realType)) {
      return;
    }

    // Keeps track of the parent paths of the elements, e.g. Observation.component
    final Deque<String> parents = new ArrayDeque<>();
    parents.add("");

    for (ElementDefinition ed : sd.getSnapshot().getElement()) {
      // Special case: root node in nested
      if (nested && !ed.hasType()) {
        continue;
      }

      boolean isValueX = isValueX(ed);

      if (isValueX) {
        // Process for each type, replacing path with actual type
        for (TypeRefComponent trc : ed.getType()) {
          String path = removeX(getPath(ed, nested)) + capitaliseFirst(trc.getCode());
          processElementDefinition(cs, nested, prefix, parents, ed, path, trc, isResource);
        }
      } else {
        String path = getPath(ed, nested);
        processElementDefinition(cs, nested, prefix, parents, ed, path, null, isResource);
      }
    }
  }

  private void processElementDefinition(CodeSystem cs, boolean nested, String prefix,
                                        final Deque<String> parents, ElementDefinition ed, String path, TypeRefComponent trc,
                                        boolean isResource) {
    String currentParent = parents.peek();

    // Look for the right parent
    if (!path.isEmpty()) {
      while (true) {
        assert currentParent != null;
        if (path.startsWith(currentParent)) break;
        parents.pop();
        currentParent = parents.peek();
      }
    }

    // Create the code
    String parent;
    if (prefix != null && !prefix.isEmpty()) {
      if (currentParent != null && !currentParent.isEmpty()) {
        parent = prefix + "." + currentParent;
      } else {
        parent = prefix;
      }
    } else {
      parent = currentParent;
    }

    if (trc == null) {
      trc = ed.getTypeFirstRep();
      if (ed.getType().size() > 1) {
        throw new RuntimeException("Found non-x-value with more than one type: " + ed);
      }
    }

    String trcCode = trc.getCode();

    ConceptDefinitionComponent cdc =
      createCode(cs, path, ed.getMin(), ed.getMax(), parent, prefix, nested, trcCode, ed,
        isResource);

    // Special case: reference types - add target profile properties
    if ("Reference".equals(trcCode)) {
      for (CanonicalType targetProfile : trc.getTargetProfile()) {
        cdc.addProperty()
          .setCode("targetProfile")
          .setValue(new StringType(targetProfile.getValueAsString()));
      }
    } else if (!"Resource".equals(trcCode) && isComplexType(trc)) {
      StructureDefinition nestedSd = getComplexType(trc);
      assert prefix != null;
      String pre = prefix + (prefix.isEmpty() ? "" : ".") + path;
      processStructureDefinition(cs, nestedSd, true, pre, isResource);
    }

    // Now adjust the path
    assert currentParent != null;
    if (path.length() > currentParent.length() && path.startsWith(currentParent)) {
      parents.push(path);
    }
  }

  /**
   * Returns the path for an element definition.
   *
   * @param ed The element definition.
   * @param nested Flag to indicate if the element definition is nested.
   * @return The path for the element definition.
   */
  private String getPath (ElementDefinition ed, boolean nested) {
    String path = nested ? discardBaseType(ed.getPath()) : ed.getPath();
    if (!path.isEmpty() && (path.contains(".") || !Character.isUpperCase(path.charAt(0)))) {
      String max = ed.getMax();
      if (!"1".equals(max)) {
        path = path + "[]";
      }
    }
    return path;
  }

  private String discardBaseType(String path) {
    int dotIndex = path.indexOf(".");
    if (dotIndex == -1) {
      return "";
    } else {
      return path.substring(dotIndex + 1);
    }
  }

  private String removeAllBrackets (String s) {
    return s.replace("[]", "");
  }

  private ConceptDefinitionComponent createCode (CodeSystem cs, String path, int min, String max,
                                                 String parentCode, String prefix, boolean nested, String type, ElementDefinition ed,
                                                 boolean isResource) {
    String code = prefix + (nested ? "." : "") +  path;

    ConceptDefinitionComponent cdc =
      cs.addConcept()
        .setCode(removeAllBrackets(code))
        .setDisplay(removeAllBrackets(code));
    cdc.addProperty().setCode("min").setValue(new IntegerType(min));
    cdc.addProperty().setCode("max").setValue(new StringType(max));
    if (parentCode != null && !parentCode.isEmpty()) {
      cdc.addProperty().setCode("parent").setValue(new CodeType(removeAllBrackets(parentCode)));
      allParents.add(removeAllBrackets(parentCode));
    } else {
      if (isResource) {
        cdc.addProperty().setCode("parent").setValue(new CodeType("DomainResource"));
        allParents.add("DomainResource");
      } else {
        cdc.addProperty().setCode("parent").setValue(new CodeType("Element"));
        allParents.add("Element");
      }
    }
    cdc.addProperty().setCode("root").setValue(new BooleanType(false));
    cdc.addProperty().setCode("deprecated").setValue(new BooleanType(false));

    allCodes.add(removeAllBrackets(code));

    // Add synonyms
    addSynonym(code, cdc);
    addSynonyms(code, 0, cdc);

    if (type != null) {
      this.pathToTypeMap.put(cdc.getCode(), type);
      cdc.addProperty().setCode("type").setValue(new StringType(type));
    } else {
      // Cannot resolve type until processing everything because it can be a content reference
      missingTypes.add(new ConceptDefinitionComponentElementDefinitionPair(cdc, ed));
    }

    return cdc;
  }

  private void addSynonyms (String code, int start, ConceptDefinitionComponent cdc) {
    int index = code.indexOf("[]", start);
    if (index == -1) {
      return;
    }

    addSynonyms(code, index + 2, cdc);
    String newCode = code.substring(0, index) +
      ((index + 2) <= code.length() ? code.substring(index + 2) : "");
    addSynonyms(newCode, index, cdc);

    // The code with no brackets is the display so no need to add
    if (newCode.contains("[]")) {
      addSynonym(newCode, cdc);
    }
  }

  private void addSynonym (String code, ConceptDefinitionComponent cdc) {
    cdc.addDesignation()
      .setValue(code)
      .getUse()
      .setSystem("http://snomed.info/sct")
      .setCode("900000000000013009")
      .setDisplay("Synonym (core metadata concept)");
  }

  private boolean isComplexType(TypeRefComponent trc) {
    String code = trc.getCode();
    String fhirpathBase = "http://hl7.org/fhirpath/";
    if (code == null || code.startsWith(fhirpathBase) || primitiveTypeMap.containsKey(code)
      || "BackboneElement".equals(code) || "Element".equals(code)) {
      return false;
    } else if (complexTypeMap.containsKey(code) || resourceMap.containsKey(code)) {
      return true;
    } else {
      throw new RuntimeException("Unexpected type " + code);
    }
  }

  private StructureDefinition getComplexType(TypeRefComponent trc) {
    String code = trc.getCode();
    if (complexTypeMap.containsKey(code)) {
      return complexTypeMap.get(code);
    } else if (resourceMap.containsKey(code)) {
      return resourceMap.get(code);
    } else {
      throw new RuntimeException("Unexpected type " + code);
    }
  }

  private String removeX(String path) {
    return path.replace("[x]", "");
  }

  private String capitaliseFirst(String s) {
    if (s.isEmpty()) {
      return s;
    }
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  static class ConceptDefinitionComponentElementDefinitionPair {
    private final ConceptDefinitionComponent cdc;
    private final ElementDefinition ed;
    public ConceptDefinitionComponentElementDefinitionPair(ConceptDefinitionComponent cdc,
                                                           ElementDefinition ed) {
      super();
      this.cdc = cdc;
      this.ed = ed;
    }
    public ConceptDefinitionComponent getCdc() {
      return cdc;
    }
    public ElementDefinition getEd() {
      return ed;
    }
  }
}
