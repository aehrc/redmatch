/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.exporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import au.csiro.redmatch.model.RedmatchProject;
import au.csiro.redmatch.model.grammar.GrammarObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.DateType;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.IntegerType;
import org.hl7.fhir.r4.model.MarkdownType;
import org.hl7.fhir.r4.model.OidType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.UriType;
import org.hl7.fhir.r4.model.UrlType;
import org.hl7.fhir.r4.model.UuidType;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.HawickJamesSimpleCycles;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import au.csiro.redmatch.model.Mapping;
import au.csiro.redmatch.model.Metadata;
import au.csiro.redmatch.model.Row;
import au.csiro.redmatch.model.Field.FieldType;
import au.csiro.redmatch.model.Field.TextValidationType;
import au.csiro.redmatch.model.grammar.redmatch.Attribute;
import au.csiro.redmatch.model.grammar.redmatch.AttributeValue;
import au.csiro.redmatch.model.grammar.redmatch.BooleanValue;
import au.csiro.redmatch.model.grammar.redmatch.CodeLiteralValue;
import au.csiro.redmatch.model.grammar.redmatch.CodeSelectedValue;
import au.csiro.redmatch.model.grammar.redmatch.ConceptLiteralValue;
import au.csiro.redmatch.model.grammar.redmatch.ConceptSelectedValue;
import au.csiro.redmatch.model.grammar.redmatch.ConceptValue;
import au.csiro.redmatch.model.grammar.redmatch.Document;
import au.csiro.redmatch.model.grammar.redmatch.DoubleValue;
import au.csiro.redmatch.model.grammar.redmatch.FieldValue;
import au.csiro.redmatch.model.grammar.redmatch.IntegerValue;
import au.csiro.redmatch.model.grammar.redmatch.ReferenceValue;
import au.csiro.redmatch.model.grammar.redmatch.Resource;
import au.csiro.redmatch.model.grammar.redmatch.Rule;
import au.csiro.redmatch.model.grammar.redmatch.StringValue;
import au.csiro.redmatch.util.FitbitUrlValidator;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;

/**
 * Used the transformation rules to transform REDCap data into FHIR resources.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Component
public class FhirExporter {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(FhirExporter.class);
  
  private final SimpleDateFormat dateYmdFormat = new SimpleDateFormat("yyyy-MM-dd");

  private final SimpleDateFormat dateTimeYmdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private final SimpleDateFormat dateTimeSecondsYmdFormat = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss");

  private final Pattern codePattern = Pattern.compile("[^\\s]+([\\s]?[^\\s]+)*");

  @Value("${redmatch.targetFolder}")
  private String targetFolderName;
  
  private Path targetFolder;
  
  @Autowired
  private HapiReflectionHelper helper;
  
  @Autowired
  private FhirContext ctx;
  
  /**
   * Checks that the target folder is configured properly.
   */
  @PostConstruct
  public void init() {
    boolean valid = true;
    if (targetFolderName != null) {
      log.info("Validating target folder " + targetFolderName);
      Path path = Paths.get(targetFolderName);
      try {
        targetFolder = Files.createDirectories(path);
      } catch (IOException e) {
        log.fatal("Unable to create target folder", e);
        valid = false;
      }
    } else {
      log.fatal("Target folder is null.");
      valid = false;
    }
    
    if (!valid) {
      throw new RuntimeException("Target folder is invalid. Please set property "
          + "redmatch.targetFolder.");
    }
  }

  /**
   * Exports a bundle with all the clinical resources (i.e. all the non-terminology resources).
   * 
   * @param project The Redmatch project.
   * @param rulesDocument The mapping rules.
   * @param mappings The mappings from REDCap fields to codes in a terminology.
   * @param rows The REDCap data. 
   * 
   * @return A bundle with the generated resources.
   */
  @Transactional
  public Bundle createClinicalBundle(RedmatchProject project, Document rulesDocument,
      List<Mapping> mappings, List<Row> rows) {
    final Bundle res = new Bundle();
    res.setType(BundleType.TRANSACTION);
    final Map<String, DomainResource> m = createClinicalResourcesFromRules(project, rulesDocument, rows);
    for (String key : m.keySet()) {
      final DomainResource dr = m.get(key);
      res.addEntry().setResource(dr).setRequest(new BundleEntryRequestComponent()
          .setMethod(HTTPVerb.PUT).setUrl(dr.fhirType() + "/" + key));
    }
    return res;
  }
  
  /**
   * Saves all the generated FHIR resources in ND-JSON format in a folder. Each file the same
   * resource type. A map of resource types and filenames is returned.
   * 
   * @param project The Redmatch project.
   * @param rulesDocument The compiled rules document.
   * @param rows The REDCap data. 
   * @return A map of resource types and files where these resources were saved.
   * @throws IOException 
   * @throws DataFormatException 
   */
  public Map<String, String> saveResourcesToFolder(RedmatchProject project, Document rulesDocument, List<Row> rows)
          throws DataFormatException, IOException {
    
    // Create folder if it doesn't exist
    Path tgtDir = Files.createDirectories(targetFolder.resolve(Path.of(project.getId())));
    
    // Tries to delete any old files in there
    for(File file: targetFolder.toFile().listFiles()) {
      if (!file.isDirectory() && file.getAbsolutePath().endsWith(".ndjson")) {
        if (file.delete()) {
          log.debug("Deleted file " + file.getAbsolutePath());
        } else {
          log.debug("Unable to deleted file " + file.getAbsolutePath());
        }
      }
    }
    
    final Map<String, DomainResource> map = createClinicalResourcesFromRules(project, rulesDocument, rows);
    
    // Group resources by type
    final Map<String, List<DomainResource>> grouped = new HashMap<>();
    for (String key : map.keySet()) {
      DomainResource dr = map.get(key);
      String resourceType = dr.getResourceType().toString();
      List<DomainResource> list = grouped.get(resourceType);
      if (list == null) {
        list = new ArrayList<>();
        grouped.put(resourceType, list);
      }
      list.add(dr);
    }
    
    // Save to folder in ND-JSON
    final Map<String, String> res = new HashMap<>();
    IParser jsonParser = ctx.newJsonParser();
    for (String key : grouped.keySet()) {
      File f = new File(tgtDir.toFile(), key + ".ndjson");
      res.put(key, f.getAbsolutePath());
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
        for(DomainResource dr : grouped.get(key)) {
          jsonParser.encodeResourceToWriter(dr, bw);
          bw.newLine();
        }
      }
    }
    return res;
  }

  /**
   * Returns a vertex in the graph. Should only be called after checking the graph contains the vertex.
   *
   * @param g The graph.
   * @param resourceType The resource type of a vertex.
   * @param resourceId The resource id of a vertex.
   * @return The vertex in the graph.
   */
  private ResourceNode getVertex(Graph<ResourceNode, DefaultEdge> g, 
      String resourceType, String resourceId) {
    for (ResourceNode v : g.vertexSet()) {
      if(v.type.equals(resourceType) && v.id.equals(resourceId)) {
        return v;
      }
    }
    throw new RuntimeException("Resource node was null. This should never happen!");
  }

  /**
   * Creates FHIR resources based on data from REDCap, mappings and a set of rules. Returns a map,
   * indexed by resource id.
   * 
   * @param project The Redmatch project.
   * @param rulesDocument The mapping rules.
   * @param rows The REDCap data.
   * 
   * @return The map of created resources, indexed by resource id.
   */
  @Transactional
  public Map<String, DomainResource> createClinicalResourcesFromRules(RedmatchProject project, Document rulesDocument,
                                                                      List<Row> rows) {
    final Map<String, DomainResource> fhirResourceMap = new HashMap<>();
    final String uniqueField = project.getUniqueFieldId();
    
    /*
     * We need to decide if an instance of a resource is created for every patient (i.e., for every
     * row in the data) or just once. This depends on the rules. If all the rules that refer to a
     * resource (with a specific id) never reference any values in the form, then a single resource
     * is created. If the rules have references to other resources then the decision depends on the
     * other resources.
     * 
     * This means that we need to create a graph to:
     *   - Check that there are no cycles and throw an error if one is detected
     *   - Traverse the graph and look for all the nodes that are reference to check if all of them
     *   are independent of the rows.
     */
    final Graph<ResourceNode, DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
    if (rulesDocument != null) {
      
      // First add all vertices
      for (Rule rule : rulesDocument.getRules()) {
        for (Resource r : rule.getResources()) {
          // Extract resource and create node
          ResourceNode rn = new ResourceNode(r);
          if (g.containsVertex(rn)) {
            rn = getVertex(g, rn.type, rn.id);
          } else {
            g.addVertex(rn);
          }
          // We might be merging various resources from multiple rules
          GrammarObject.DataReference ndr = r.referencesData();
          GrammarObject.DataReference odr = rn.referenceData;
          if (odr == null) {
            // If referenceData hasn't been set then just use this one
            rn.referenceData = ndr;
          } else {
            if (odr.equals(GrammarObject.DataReference.NO) && !ndr.equals(GrammarObject.DataReference.NO)) {
              rn.referenceData = ndr;
            } else if (odr.equals(GrammarObject.DataReference.RESOURCE)
                    && ndr.equals(GrammarObject.DataReference.YES)) {
              rn.referenceData = ndr;
            }
          }
        }
      }
      
      // Then add all edges
      for (Rule rule : rulesDocument.getRules()) {
        for (Resource r : rule.getResources()) {
          ResourceNode rn = getVertex(g, r.getResourceType(), r.getResourceId());
          // Get references and create edges
          for (ReferenceValue rv : r.getReferences()) {
            ResourceNode tn = new ResourceNode(rv.getResourceType(), rv.getResourceId());
            if (g.containsVertex(tn)) {
              tn = getVertex(g, tn.type, tn.id);
            } else {
              throw new RuntimeException("Vertex " + tn 
                  + " is not in graph. This should never happen!");
            }
            g.addEdge(rn, tn);
          }
        }
      }
    }

    /*
     * At this point we have a directed graph and need to do the following:
     * 1. Check for cycles and throw an error if one is detected.
     * 2. Do an inverse topological sort so that we can do 3 and also so resources can be created in the correct order.
     * 3. Set the definitive value of referenceData for vertices.
     */
    HawickJamesSimpleCycles<ResourceNode, DefaultEdge> cd = new HawickJamesSimpleCycles<>(g);
    if (cd.countSimpleCycles() > 0) {
      String firstCycle = cd.findSimpleCycles().get(0).stream().map(v -> "(" + v.type + "<" + v.id + ">")
              .collect(Collectors.joining(", "));
      throw new RuleApplicationException("Found an illegal cycle in the rule definitions: " + firstCycle);
    }

    // Do inverse topological sort
    final List<ResourceNode> sortedNodes = new ArrayList<>();
    GraphIterator<ResourceNode, DefaultEdge> it = new TopologicalOrderIterator<>(g);
    while (it.hasNext()) {
      sortedNodes.add(it.next());
    }
    Collections.reverse(sortedNodes);

    // Set definitive value of referenceData for vertices
    for (ResourceNode rn : sortedNodes) {
      if (rn.referenceData == null) {
        throw new RuntimeException("Reference data in resource node " + rn + " is null.");
      }
      
      // If referenceData is REFERENCE then look for all references and update value
      if (rn.referenceData.equals(GrammarObject.DataReference.RESOURCE)) {
        int refCount = 0;
        boolean foundYes = false;
        it = new DepthFirstIterator<>(g, rn);
        while (it.hasNext()) {
          refCount++;
          if (it.next().referenceData.equals(GrammarObject.DataReference.YES)) {
            rn.referenceData = GrammarObject.DataReference.YES;
            foundYes = true;
            break;
          }
        }
        if (refCount == 0) {
          throw new RuntimeException("Malformed graph. Node " + rn.type + "<" + rn.id + "> has no references. " +
                  "This should never happen!");
        }
        if (!foundYes) {
          rn.referenceData = GrammarObject.DataReference.NO;
        }
      }
    }

    // Populate a set with the ids of unique resources - needed to create the right references to them
    final Set<String> uniqueIds = new HashSet<>();
    for (ResourceNode rn : g.vertexSet()) {
      if (rn.referenceData.equals(GrammarObject.DataReference.NO)) {
        uniqueIds.add(rn.toString());
      }
    }
    log.debug("Found the following unique resources: " + uniqueIds);

    // Iterate over resources and first create only the ones that do not depend on data
    for (ResourceNode rn : sortedNodes) {
      if (rn.referenceData.equals(GrammarObject.DataReference.NO)) {
        for (Rule rule : getReferencingRules(rn, rulesDocument)) {
          for (Resource r : rule.getResourcesToCreate(project, null)) {
            // Only create this resource - rules might create more that one resource
            if (rn.equalsResource(r)) {
              createResource(r, fhirResourceMap, project, null, null, uniqueIds);
            }
          }
        }
      }
    }
    
    // Now create the resource that depend on the data
    for (Row row : rows) {
      final Map<String, String> data = row.getData();
      String recordId = data.get(uniqueField);
      if (recordId == null) {
        throw new RuntimeException(
            "Expected " + uniqueField + " but was null. This should not happen!");
      }

      for (ResourceNode rn : sortedNodes) {
        if (rn.referenceData.equals(GrammarObject.DataReference.YES)) {
          for (Rule rule : getReferencingRules(rn, rulesDocument)) {
            for (Resource r : rule.getResourcesToCreate(project, data)) {
              // Only create this resource - rules might create more that one resource
              if (rn.equalsResource(r)) {
                createResource(r, fhirResourceMap, project, data, recordId, uniqueIds);
              }
            }
          }
        }
      }
    }

    // Prune resources to get rid of empty values in lists
    for (DomainResource c : fhirResourceMap.values()) {
      prune(c);
    }

    return fhirResourceMap;
  }

  /**
   * Returns a collection of rules that create a resource. A rule can create more than one resource.
   *
   * @param node The resource node that specifies the resource we are interested in.
   * @param rulesDocument The document that contains all the rules.
   * @return A collection of rules that create this resource.
   */
  private Collection<Rule> getReferencingRules(ResourceNode node, Document rulesDocument) {
    Collection<Rule> res = new ArrayList<>();
    if (rulesDocument != null) {
      for (Rule r : rulesDocument.getRules()) {
        for (Resource resource : r.getResources()) {
          if (node.equalsResource(resource)) {
            res.add(r);
          }
        }
      }
    }
    return res;
  }

  /**
   * Creates a resource and populates it's attributes.
   * 
   * @param resource The internal resource representation.
   * @param fhirResourceMap The FHIR resource map.
   * @param project The Redmatch project.
   * @param data The row of REDCap data.
   * @param recordId The id of this record. Used to create the FHIR ids.
   * @param uniqueIds Set of resources that have a single instance.
   */
  @Transactional
  private void createResource(Resource resource, Map<String, DomainResource> fhirResourceMap, RedmatchProject project,
      Map<String, String> data, String recordId, Set<String> uniqueIds) {
    final String resourceId = resource.getResourceId();
    final String fhirId = resourceId + (recordId != null ? ("-" + recordId) : "");

    DomainResource fhirResource = fhirResourceMap.get(fhirId);
    if (fhirResource == null) {
      Object instance = null;
      try {
        instance = Class.forName(HapiReflectionHelper.FHIR_TYPES_BASE_PACKAGE + "." 
            + resource.getResourceType()).getConstructor().newInstance();
      } catch (ClassNotFoundException | IllegalAccessException | InstantiationException 
          | IllegalArgumentException | InvocationTargetException | NoSuchMethodException 
          | SecurityException e) {
        throw new RuleApplicationException("Unable to create resource " + resource.getResourceType(), e);
      }
      fhirResource = (DomainResource) instance;
      fhirResource.setId(fhirId);
      fhirResourceMap.put(fhirId, fhirResource);
    }
    for (AttributeValue attVal : resource.getResourceAttributeValues()) {
      setValue(fhirResource, attVal.getAttributes(), attVal.getValue(), project, data,
          recordId, uniqueIds, fhirResourceMap);
    }
  }
  
  private boolean isValueX(Child hapiMetadata, Class<? extends Base> c) {
    // Special case: extensions
    if (c.getName().endsWith("Extension") && hapiMetadata.name().equals("value")) {
      return true;
    }
    return hapiMetadata.type().length > 1;
  }
  
  /**
   * Assigns the value to the specified attribute of the resource. Tries to accommodate the types but
   * might fail if the specified value is incompatible with the attribute type.
   * 
   * @param resource The FHIR resource where the value is going to be set.
   * @param attributes A list of {@link Attribute}. This represents a single attribute that might be
   *        several levels down. The list represents the path to the attribute.
   * @param value The value to set.
   * @param project The Redmatch project.
   * @param row The row of data to be used to set this value.
   * @param recordId The id of this record. Used to create the FHIR ids.
   * @param uniqueIds Set of resource ids that have a single instance.
   * @param fhirResourceMap Map of FHIR resource created so far.
   */
  @Transactional
  private void setValue(DomainResource resource, List<Attribute> attributes,
      au.csiro.redmatch.model.grammar.redmatch.Value value, RedmatchProject project,
      Map<String, String> row, String recordId, Set<String> uniqueIds,
      Map<String, DomainResource> fhirResourceMap) {

    // Get chain of attribute names
    final List<Attribute> attributesCopy = new ArrayList<>();
    attributesCopy.addAll(attributes);
    final Attribute leafAttribute = attributesCopy.remove(attributes.size() - 1);
    final String leafAttributeName = leafAttribute.getName();
    final Integer index = leafAttribute.getAttributeIndex();

    try {
      // Now we need to find or create the object where the value is going to be set
      final Base theElement = helper.getElementToSet(resource, attributesCopy);

      // Now we need to get the value to set
      Base theValue = null;
      final Field f = helper.getField(theElement.getClass(), leafAttributeName);
    
      // We use the generated annotations in the FHIR model to get the type
      final Child hapiMetadata = f.getAnnotation(Child.class);
      final boolean isValueX = isValueX(hapiMetadata, theElement.getClass());
      final Class<?> fhirType = getTypeFromHapiAnnotations(hapiMetadata, f, leafAttributeName);
     // boolean isList = hapiMetadata.max() > 1;
      
      // Special case for codes
      Class<?> enumFactory = null;
      if (fhirType.equals(CodeType.class) || fhirType.equals(Enumeration.class)) {
        Class<?> hapiType = helper.getParametrisedType(f);
        
        // Now we need the EnumFactory for this type
        log.debug("Getting EnumFactory for type " + hapiType.getName());
        try {
          enumFactory = Class.forName(hapiType.getName() + "EnumFactory");
        } catch (ClassNotFoundException e) {
          throw new RuleApplicationException("Unable to get EnumFactory for class " + hapiType);
        }
      }

      theValue = getValue(value, fhirType, project, row, recordId, enumFactory, uniqueIds, fhirResourceMap);
      if (theValue == null) {
        throw new RuleApplicationException("Unable to get value: " + value);
      }
      
      helper.invokeSetter(theElement, leafAttributeName, theValue, leafAttribute.isList(), index, 
          isValueX);
    } catch (RuleApplicationException | NoMappingFoundException e) {
      throw e;
    } catch (Exception e) {
      handleReflectionException(e);
    }
  }
  
  private void validateMappingForConcept(Mapping m) {
    String system = m.getTargetSystem();
    String code = m.getTargetCode();
    
    if (system == null || system.isBlank() || code == null || code.isBlank()) {
      throw new NoMappingFoundException("Mapping to a concept for field " + m.getRedcapLabel() 
        + " (" + m.getRedcapFieldId() + ") is required but was not found.");
    }
  }
  
  private void validateMappingForCode(Mapping m) {
    String code = m.getTargetCode();
    
    if (code == null) {
      throw new NoMappingFoundException("Mapping to a code for field " + m.getRedcapLabel() 
        + " (" + m.getRedcapFieldId() + ") is required but was not found.");
    }
  }
  
  /**
   * Resolves a value.
   * 
   * @param value The value specified in the transformation rules.
   * @param fhirType The type of the FHIR attribute where this value will be set.
   * @param project The Redmatch project.
   * @param row The row of data from REDCap.
   * @param recordId The id of this record. Used to create the references to FHIR ids.
   * @param enumFactory If the type is an enumeration, this is the fatory to create an instance.
   * @param uniqueIds Set of resource ids that have a single instance.
   * @param fhirResourceMap Map of FHIR resource created so far.
   * @return The value or null if the value cannot be determined. This can also be a list.
   */
  @Transactional
  private Base getValue(au.csiro.redmatch.model.grammar.redmatch.Value value, Class<?> fhirType,
      RedmatchProject project, Map<String, String> row, String recordId,
      Class<?> enumFactory, Set<String> uniqueIds, Map<String, DomainResource> fhirResourceMap) {
    if(value instanceof BooleanValue) {
      return new BooleanType(((BooleanValue) value).getValue());
    } else if (value instanceof CodeLiteralValue) {
      String code = ((CodeLiteralValue) value).getCode();
      return getCode(code, fhirType, enumFactory);
    } else if (value instanceof ConceptLiteralValue) {
      ConceptLiteralValue clv = (ConceptLiteralValue) value;
      String system = clv.getSystem();
      String code = clv.getCode();
      String display = clv.getDisplay() != null ? clv.getDisplay() : "";
      
      return getConcept(system, code, display, fhirType);
    } else if (value instanceof DoubleValue) {
      return new DecimalType(((DoubleValue) value).getValue());
    } else if (value instanceof IntegerValue) {
      return new IntegerType(((IntegerValue) value).getValue());
    } else if (value instanceof ReferenceValue) {
      ReferenceValue rv = (ReferenceValue) value;
      Reference ref = new Reference();
      
      String resourceType = rv.getResourceType();
      String resourceId = rv.getResourceId();
      String resId = resourceType + "<" + resourceId + ">";
      boolean unique = uniqueIds.contains(resId);

      if (unique) {
        // This is a reference to a unique resource - no need to append row id
        if (fhirResourceMap.containsKey(resourceId)) {
          ref.setReference("/" + resourceType + "/" + resourceId);
        } else {
          log.debug("Did not find resource " + resourceType + "/" + resourceId);
        }
      } else {
        if (fhirResourceMap.containsKey(resourceId + "-" + recordId)) {
          ref.setReference("/" + resourceType + "/" + resourceId + "-" + recordId);
        } else {
          log.debug("Did not find resource " + resourceType + "/" + resourceId + "-" + recordId);
        }
      }
      return ref;
    } else if (value instanceof StringValue) {
      if (fhirType.equals(StringType.class)) {
        return new StringType(((StringValue) value).getStringValue());
      } else if (fhirType.equals(MarkdownType.class)) {
        return new MarkdownType(((StringValue) value).getStringValue());
      } else if (fhirType.equals(IdType.class)) {
        return new IdType(((StringValue) value).getStringValue());
      } else if (fhirType.equals(UriType.class)) {
        return new UriType(((StringValue) value).getStringValue());
      } else if (fhirType.equals(OidType.class)) {
        return new OidType(((StringValue) value).getStringValue());
      } else if (fhirType.equals(UuidType.class)) {
        return new UuidType(((StringValue) value).getStringValue());
      } else if (fhirType.equals(CanonicalType.class)) {
        return new CanonicalType(((StringValue) value).getStringValue());
      } else if (fhirType.equals(UrlType.class)) {
        return new UrlType(((StringValue) value).getStringValue());
      } else {
        throw new RuleApplicationException("Got StringValue for FHIR type " + fhirType.getName() 
          + ". This should not happen!");
      }
    } else if (value instanceof CodeSelectedValue) {
      CodeSelectedValue csv = (CodeSelectedValue) value;
      String fieldId = csv.getFieldId();
      Mapping m = getSelectedMapping(fieldId, row, project);
      validateMappingForCode(m);
      return getCode(m.getTargetCode(), fhirType, enumFactory);
    } else if (value instanceof ConceptSelectedValue) {
      ConceptSelectedValue csv = (ConceptSelectedValue) value;
      String fieldId = csv.getFieldId();
      Mapping m = getSelectedMapping(fieldId, row, project);
      validateMappingForConcept(m);
      
      return getConcept(m.getTargetSystem(), m.getTargetCode(), m.getTargetDisplay(), fhirType);
    } else if (value instanceof ConceptValue) {
      // Ontoserver REDCap plugin format: 74400008|Appendicitis|http://snomed.info/sct
      ConceptValue cv = (ConceptValue) value;
      String fieldId = cv.getFieldId();
      au.csiro.redmatch.model.Field f = project.getField(fieldId);
      
      switch (f.getFieldType()) {
      case CHECKBOX:
      case DROPDOWN:
      case TRUEFALSE:
      case YESNO:
      case RADIO:
      case CHECKBOX_OPTION:
      case DROPDOW_OR_RADIO_OPTION:
        // In this case we look for a mapping to the field itself, not its options
        Mapping m = findMapping(project.getMappings(), fieldId);
        validateMappingForConcept(m);
        return getConcept(m.getTargetSystem(), m.getTargetCode(), m.getTargetDisplay(), fhirType);
      case TEXT:
        // This is a special case where a text field is connected to the REDCap Ontoserver plugin
        String val = getValue(row, fieldId);
        if(TextValidationType.FHIR_TERMINOLOGY.equals(f.getTextValidationType())) {
          String[] parts = val.split("[|]");
          if (parts.length != 3) {
            throw new RuleApplicationException("Expected CONCEPT value to have the format "
                + "code|display|system but got " + val);
          }
          return getConcept(parts[2].trim(), parts[0].trim(), parts[1].trim(), fhirType);
        } else {
          // Deal with plain text
          // Find the mapping
          Map<String, Mapping> map = findMappings(project.getMappings(), fieldId);
          m = map.get(val);
          
          // We don't validate here because it is ok if the mapping is not filled out
          String system = m.getTargetSystem();
          String code = m.getTargetCode();
          String display = m.getTargetDisplay();
          
          if (system != null && !system.isBlank() && code != null && !code.isBlank()) {
            return getConcept(system, code, display, fhirType);
          } else {
            return new CodeableConcept().setText(val);
          }
        }
      case FILE:
      case NOTES:
      case DESCRIPTIVE:
      case SLIDER:
      case SQL:
      case UNKNOWN:
      case CALC:
      default:
        throw new RuleApplicationException("REDCap field type " + f.getFieldType() + " is not "
            + "supported in a CONCEPT expression (field: " + fieldId + ")");
      }
    } else if (value instanceof FieldValue) {
      FieldValue fv = (FieldValue) value;
      String fieldId = fv.getFieldId();
      String val = getValue(row, fieldId);
      au.csiro.redmatch.model.Field f = project.getField(fieldId);
      
      switch (f.getFieldType()) {
      case TEXT:
        if (f.hasTextValidationType()) {
          switch (f.getTextValidationType()) {
          case DATETIME_DMY:
            return getDate(val, fhirType, dateTimeYmdFormat);
          case DATETIME_MDY:
            return getDate(val, fhirType, dateTimeYmdFormat);
          case DATETIME_SECONDS_DMY:
            return getDate(val, fhirType, dateTimeSecondsYmdFormat);
          case DATETIME_SECONDS_MDY:
            return getDate(val, fhirType, dateTimeSecondsYmdFormat);
          case DATETIME_SECONDS_YMD:
            return getDate(val, fhirType, dateTimeSecondsYmdFormat);
          case DATETIME_YMD:
            return getDate(val, fhirType, dateTimeYmdFormat);
          case DATE_DMY:
            return getDate(val, fhirType, dateYmdFormat);
          case DATE_MDY:
            return getDate(val, fhirType, dateYmdFormat);
          case DATE_YMD:
            return getDate(val, fhirType, dateYmdFormat);
          case EMAIL:
            return getString(val, fhirType, "EMAIL");
          case INTEGER:
            return getInteger(val, fhirType);
          case NONE:
            return getString(val, fhirType, "TEXT");
          case NUMBER:
            return getDecimal(val, fhirType);
          case PHONE:
            return getString(val, fhirType, "PHONE");
          case TIME:
            // The constructor takes a string of the form HH:mm:ss[.SSSS] and the REDCap field is
            // HH:mm
            return new TimeType(val + ":00");
          case ZIPCODE:
            return getString(val, fhirType, "ZIPCODE");
          default:
            throw new RuleApplicationException("Unknown text validation type " 
                + f.getTextValidationType());
          }
        } else {
          return getString(val, fhirType, "TEXT");
        }
      case CALC:
        // Calculations are always numbers
        return getDecimal(val, fhirType);
      case NOTES:
        return getString(val, fhirType, "NOTES");
      case RADIO:
      case DROPDOWN:
      case CHECKBOX:
      case TRUEFALSE:
      case YESNO:
      case CHECKBOX_OPTION:
      case DROPDOW_OR_RADIO_OPTION:
      case SLIDER:
      case DESCRIPTIVE:
      case FILE:
      case SQL:
      case UNKNOWN:
      default:
        throw new RuleApplicationException("REDCap field type " + f.getFieldType() + " is not "
            + "supported in a VALUE expression(field: " + fieldId + ")");
      }
    } else {
      throw new RuleApplicationException("Unable to get VALUE for " + value);
    }
  }

  /**
   * Removes any empty attributes that might have been created because of the rules.
   * 
   * @param base The resource to prune.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void prune(Base base) {
    // Find attributes where values are set
    final Set<String> setAttrs = new HashSet<>();
    Class<? extends Base> c = base.getClass();
    for (Method m : c.getMethods()) {
      try {
        String methodName = m.getName();
        if (methodName.startsWith("has") && Character.isUpperCase(methodName.charAt(3))
            && m.getParameterCount() == 0 && ((Boolean) m.invoke(base, new Object[0]))) {
          setAttrs.add(m.getName().substring(3));
        }
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuleApplicationException("There was a reflection issue while pruning resources.",
            e);
      }
    }

    // Get set attributes with multiplicity > 1
    final Set<String> multSetAttrs = new HashSet<>();
    for (Method m : c.getMethods()) {
      if (m.getName().startsWith("add") && setAttrs.contains(m.getName().substring(3))) {
        multSetAttrs.add(m.getName().substring(3));
      }
    }

    // Prune those attributes
    for (String att : multSetAttrs) {
      try {
        List<Base> list = (List<Base>) c.getMethod("get" + att, new Class<?>[0]).invoke(base,
            new Object[0]);
        List<Base> toDelete = new ArrayList<>();
        for (Base b : list) {
          if (b.isEmpty()) {
            toDelete.add(b);
          }
        }
        list.removeAll(toDelete);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
          | NoSuchMethodException | SecurityException e) {
        throw new RuleApplicationException("There was a reflection issue while pruning resources.",
            e);
      }
    }

    // Prune recursively - call the method for every attribute that is set and is not a primitive
    // type
    for (String attr : setAttrs) {
      try {
        Object o = c.getMethod("get" + attr, new Class<?>[0]);
        if (o instanceof Base) {
          prune((Base) o);
        } else if (o instanceof List) {
          for (Object oo : (List) o) {
            if (oo instanceof Base) {
              prune((Base) o);
            }
          }
        }
      } catch (NoSuchMethodException | SecurityException e) {
        throw new RuleApplicationException("There was a reflection issue while pruning resources.",
            e);
      }
    }
  }
  
  private Class<?> getTypeFromHapiAnnotations(final Child hapiMetadata, Field f, 
      String leafAttributeName) throws ClassNotFoundException {
    final Class<?>[] types = hapiMetadata.type();
    final Class<?> fieldType = f.getType();
    if (Reference.class.isAssignableFrom(fieldType)) {
      // References are a special case - note that we do not return the target types
      return fieldType;
    } else if (types.length == 1) {
      
      Class<?> type = types[0];
      if (type.equals(CodeType.class) && Enumeration.class.isAssignableFrom(fieldType)) {
        // Deal with special case for codes where sometimes this is an enumeration in the API
        type = fieldType;
      }
      return type;
    } else {
      return helper.getValueXAttributeType(leafAttributeName);
      
    }
  }
  
  private Base getCode(String code, Class<?> type, Class<?> enumFactory) {
    // This can be a CodeType or an Enumeration
    if (type.isAssignableFrom(Enumeration.class)) {
      if (enumFactory != null) {
        try {
          // Create instance of factory
          Object factory = enumFactory.getDeclaredConstructor().newInstance();
          
          // Call the fromCode method
          Method fromCode = findMethodByName("fromType", enumFactory);
          return (Base) fromCode.invoke(factory, new StringType(code));
        } catch (Exception e) {
          handleReflectionException(e);
          return null;
        }
      } else {
        throw new RuleApplicationException("Type is an enumeration but the enumeration factory "
            + "is null");
      }
    } else if (type.isAssignableFrom(CodeType.class)) {
      return new CodeType (code);
    } else {
      throw new RuleApplicationException("Expected an Enumeration or CodeType but got " 
          + type.getCanonicalName());
    }
  }
  
  private Base getConcept(String system, String code, String display, Class<?> type) {
    if (!FitbitUrlValidator.isValid(system)) {
      throw new RuleApplicationException("The system '" + system + "' is invalid.");
    }

    if (!codePattern.matcher(code).matches()) {
      throw new RuleApplicationException("The code " + code + " is invalid.");
    }
    
    if (type.isAssignableFrom(Coding.class)) {
      return new Coding().setSystem(system).setCode(code).setDisplay(display);
    } else if (type.isAssignableFrom(CodeableConcept.class)) {
      return new CodeableConcept().addCoding(
          new Coding().setSystem(system).setCode(code).setDisplay(display));
    } else {
      throw new RuleApplicationException("Expected a Coding or a CodeableConcept but got " 
          + type.getCanonicalName());
    }
  }
  
  private Base getDate(String val, Class<?> fhirType, SimpleDateFormat sdf) {
    if (fhirType.isAssignableFrom(InstantType.class)) {
      return new InstantType(processDate(sdf, val));
    } else if (fhirType.isAssignableFrom(DateTimeType.class)) {
      return new DateTimeType(processDate(sdf, val));
    } else if (fhirType.isAssignableFrom(DateType.class)) {
      return new DateType(processDate(sdf, val));
    } else {
      throw new RuleApplicationException("Tried to assign REDCap DATE TIME field to FHIR "
          + "type " + fhirType.getCanonicalName() + ". Only Instant, DateTime and Date are "
              + "supported.");
    }
  }
  
  private Base getString(String val, Class<?> fhirType, String redcapFieldType) {
    if (fhirType.isAssignableFrom(StringType.class)) {
      return new StringType(val);
    } else {
      throw new RuleApplicationException("Tried to assign REDCap " + redcapFieldType + " field "
          + "to FHIR type " + fhirType.getCanonicalName() + ". Only String is supported.");
    }
  }
  
  private Base getInteger(String val, Class<?> fhirType) {
    if (fhirType.isAssignableFrom(IntegerType.class)) {
      return new IntegerType(val);
    } else if (fhirType.isAssignableFrom(StringType.class)) {
      // We allow assigning to a string field
      return new StringType(val);
    } else if (fhirType.isAssignableFrom(DecimalType.class)) {
      // We allow assigning to a decimal field
      return new DecimalType(val);
    } else {
      throw new RuleApplicationException("Tried to assign REDCap INTEGER field to FHIR type " + 
          fhirType.getCanonicalName() + ". Only Integer, Decimal and String are supported.");
    }
  }
  
  private Base getDecimal(String val, Class<?> fhirType) {
    if (fhirType.isAssignableFrom(DecimalType.class)) {
      return new DecimalType(val);
    } else if (fhirType.isAssignableFrom(StringType.class)) {
      // We allow assigning to a string field
      return new StringType(val);
    } else {
      throw new RuleApplicationException("Tried to assign REDCap NUMBER field to FHIR type " + 
          fhirType.getCanonicalName() + ". Only Decimal and String are supported.");
    }
  }

  
  
  private String getValue(Map<String, String> row, String fieldId) {
    if (row == null) {
      throw new RuntimeException("Row was null when getting value for field " + fieldId + ". This "
          + "should never happen!");
    }
    String s = row.get(fieldId);
    if (s == null) {
      throw new RuleApplicationException("Coudln't find any value for field " + fieldId + " in row "
          + row);
    }
    return s;
  }
  
  /**
   * Gets the mapping for a selected value, accounting for the differences between RADIOs, DROPDOWNs
   * and CHECKBOX_OPTIONs.
   * 
   * @param fieldId The REDCap field id.
   * @param row A row of REDCap data.
   * @param project The Redmatch project.
   * @return The mapping.
   */
  private Mapping getSelectedMapping(String fieldId, Map<String, String> row, RedmatchProject project) {
    if (row == null) {
      throw new RuntimeException("Row was null when getting selected mapping for field " 
          + fieldId + ". This should never happen!");
    }
    au.csiro.redmatch.model.Field f = project.getField(fieldId);
    FieldType ft = f.getFieldType();
    if (ft.equals(FieldType.RADIO) || ft.equals(FieldType.DROPDOWN)) {
      String val = getValue(row, fieldId);
      return findMapping(project.getMappings(), fieldId + "___" + val);
    } else if (ft.equals(FieldType.CHECKBOX_OPTION)) {
      return findMapping(project.getMappings(), fieldId);
    } else {
      throw new RuleApplicationException("Cannot get selected mapping for field of type " + ft);
    }
  }
  
  private Mapping findMapping(List<Mapping> mappings, String fieldId) {
    for (Mapping mapping : mappings) {
      if (mapping.getRedcapFieldId().equals(fieldId)) {
        return mapping;
      }
    }
    throw new RuleApplicationException("A mapping for field " + fieldId + " is required but "
        + "was not found.");
  }
  
  private Map<String, Mapping> findMappings(List<Mapping> mappings, String fieldId) {
    Map<String, Mapping> res = new HashMap<>();
    for (Mapping mapping : mappings) {
      if (mapping.getRedcapFieldId().equals(fieldId)) {
        res.put(mapping.getText(), mapping);
      }
    }
    return res;
  }
  
  /**
   * Creates a date.
   * 
   * @param sdf The date formatter to use.
   * @param stringVal The date in string format.
   * @return The date object.
   * @throws RuleApplicationException If unable to parse the date.
   */
  private Date processDate(SimpleDateFormat sdf, String stringVal) {
    Date date = null;
    try {
      date = sdf.parse(stringVal);
    } catch (ParseException e) {
      throw new RuleApplicationException("Could not parse date: " + stringVal);
    }
    return date;
  }
  
  private Method findMethodByName (String name, Class<?> clazz) {
    for (Method method : clazz.getMethods()) {
      if (method.getName().equalsIgnoreCase(name)) {
        return method;
      }
    }
    return null;
  }
  
  private void handleReflectionException(Exception e) {
    if (e instanceof NoSuchMethodException) {
      throw new RuleApplicationException(
          "A method could not be found: " + e.getLocalizedMessage(), e);
    } else if (e instanceof NoSuchFieldException) {
      throw new RuleApplicationException(
          "A field could not be found: " + e.getLocalizedMessage(), e);
    } else if (e instanceof ClassNotFoundException) {
      throw new RuleApplicationException(
          "A class could not be found: " + e.getLocalizedMessage(), e);
    } else if (e instanceof IllegalAccessException) {
      throw new RuleApplicationException(
          "There was a problem creating a field: " + e.getLocalizedMessage(), e);
    } else if (e instanceof IllegalArgumentException) {
      throw new RuleApplicationException(
          "An illegal argument was used: " + e.getLocalizedMessage(), e);
    } else if (e instanceof InvocationTargetException) {
      InvocationTargetException ite = (InvocationTargetException) e;
      Throwable cause = ite.getCause();
      throw new RuleApplicationException(
          "There was a problem invoking a method or constructor: " + 
          cause != null ? cause.getLocalizedMessage() : e.getLocalizedMessage(), e);
    } else {
      throw new RuleApplicationException ("There was a problem using reflection.", e);
    }
  }
  
  class ResourceNode {
    String type;
    String id;
    GrammarObject.DataReference referenceData;
    
    public ResourceNode(Resource r) {
      this.type = r.getResourceType();
      this.id = r.getResourceId();
    }

    public ResourceNode(String type, String id) {
      this.type = type;
      this.id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ResourceNode that = (ResourceNode) o;
      return type.equals(that.type) && id.equals(that.id);
    }

    public boolean equalsResource(Resource r) {
      if (type.equals(r.getResourceType()) && id.equals(r.getResourceId())) {
        return true;
      } else {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, id);
    }

    @Override
    public String toString() {
      return type + "<" + id + ">";
    }
  }

}
