/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.exporter;

import au.csiro.redmatch.compiler.*;
import au.csiro.redmatch.compiler.Resource;
import au.csiro.redmatch.model.Row;
import au.csiro.redmatch.util.FitbitUrlValidator;
import au.csiro.redmatch.util.GraphUtils;
import au.csiro.redmatch.util.Progress;
import au.csiro.redmatch.util.ProgressReporter;
import ca.uhn.fhir.model.api.annotation.Child;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforms REDCap data into FHIR resources using a transformation rules document.
 * 
 * @author Alejandro Metke Jimenez
 */
@Component()
@Scope("prototype")
public class FhirExporter {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(FhirExporter.class);

  private final Pattern codePattern = Pattern.compile("[^\\s]+([\\s]?[^\\s]+)*");
  
  @Autowired
  private HapiReflectionHelper helper;

  /**
   * The transformation rules document.
   */
  private final Document doc;

  /**
   * The source data.
   */
  private final List<Row> rows;

  /**
   * Set of resource ids that have a single instance (i.e., an instance is not created for every patient).
   */
  private final Set<String> uniqueIds = new HashSet<>();

  /**
   * The result of the transformation.
   */
  private final Map<String, DomainResource> fhirResourceMap = new HashMap<>();

  /**
   * Constructor.
   *
   * @param doc The transformation rules document.
   * @param rows The source data.
   */
  public FhirExporter(Document doc, List<Row> rows) {
    this.doc = doc;
    this.rows = rows;
  }

  /**
   * Constructor.
   *
   * @param doc The transformation rules document.
   * @param rows The source data.
   * @param helper The HAPI transformation helper instance.
   */
  public FhirExporter(Document doc, List<Row> rows, HapiReflectionHelper helper) {
    this(doc, rows);
    this.helper = helper;
  }

  /**
   * Creates FHIR resources based on data from the source. Returns a map, indexed by resource id.
   *
   * @param progressReporter Used to report progress.
   * @return The map of created resources, indexed by resource id.
   */
  public Map<String, DomainResource> transform(ProgressReporter progressReporter, CancelChecker cancelToken)
    throws TransformationException {
    final String uniqueField = doc.getSchema().getFields().get(0).getFieldId();
    log.debug("Transforming Redmatch project using unique field " + uniqueField);

    GraphUtils.Results res = GraphUtils.buildGraph(doc);
    if (!res.getDiagnostics().isEmpty()) {
      boolean hasErrors = false;
      StringBuilder sb = new StringBuilder();
      for(Diagnostic d : res.getDiagnostics()) {
        if (d.getSeverity().equals(DiagnosticSeverity.Error)) {
          hasErrors = true;
          sb.append(d.getMessage());
          sb.append(System.lineSeparator());
        }
      }
      if (hasErrors) {
        throw new TransformationException(sb.toString());
      }
    }

    this.uniqueIds.addAll(res.getUniqueIds());

    // Iterate over resources and first create only the ones that do not depend on data
    for (GraphUtils.ResourceNode rn : res.getSortedNodes()) {
      if (rn.getReferenceData().equals(GrammarObject.DataReference.NO)) {
        for (Rule rule : getReferencingRules(rn, doc)) {
          RedcapVisitor visitor = new RedcapVisitor(doc.getSchema(), null);
          visitor.visit(rule);
          for (Resource r : visitor.getResources()) {
            // Only create this resource - rules might create more than one resource
            if (rn.equalsResource(r)) {
              createResource(r, null, null);
            }
          }
        }
      }
    }

    try {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportStart("Transforming into FHIR"));
      }

      int totalRows = rows.size();
      double div = totalRows / 100.0;
      // Now create the resource that depend on the data
      for (int i = 0; i < rows.size(); i++) {
        Row row = rows.get(i);
        final Map<String, String> data = row.getData();
        String recordId = data.get(uniqueField);
        if (recordId == null) {
          throw new RuntimeException(
            "Expected '" + uniqueField + "' to be a field in the data. Found fields " + data.keySet()
              + ". This should not happen!");
        }
        for (GraphUtils.ResourceNode rn : res.getSortedNodes()) {
          if (rn.getReferenceData().equals(GrammarObject.DataReference.YES)) {
            for (Rule rule : getReferencingRules(rn, doc)) {
              RedcapVisitor visitor = new RedcapVisitor(doc.getSchema(), row);
              visitor.visit(rule);
              for (Resource r : visitor.getResources()) {
                // Only create this resource - rules might create more than one resource
                if (rn.equalsResource(r)) {
                  createResource(r, data, recordId);
                }
              }
            }
          }
        }
        if (progressReporter != null) {
          progressReporter.reportProgress(Progress.reportProgress((int) Math.floor(i / div)));
        }
        if(cancelToken != null && cancelToken.isCanceled()) {
          return fhirResourceMap;
        }
      }

      // Prune resources to get rid of empty values in lists
      for (DomainResource c : fhirResourceMap.values()) {
        prune(c);
      }

      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportProgress(100));
      }

      return fhirResourceMap;
    } finally {
      if (progressReporter != null) {
        progressReporter.reportProgress(Progress.reportEnd());
      }
    }
  }



  /**
   * Returns a collection of rules that create a resource. A rule can create more than one resource.
   *
   * @param node The resource node that specifies the resource we are interested in.
   * @param rulesDocument The document that contains all the rules.
   * @return A collection of rules that create this resource.
   */
  private Collection<Rule> getReferencingRules(GraphUtils.ResourceNode node, Document rulesDocument) {
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
   * Creates a resource and populates its attributes.
   * 
   * @param resource The internal resource representation.
   * @param data The row of REDCap data.
   * @param recordId The id of this record. Used to create the FHIR ids.
   */
  private void createResource(Resource resource, Map<String, String> data, String recordId) {
    final String resourceId = resource.getResourceId();
    final String fhirId = resourceId + (recordId != null ? ("-" + recordId) : "");

    DomainResource fhirResource = fhirResourceMap.get(fhirId);
    if (fhirResource == null) {
      Object instance;
      try {
        instance = Class.forName(HapiReflectionHelper.FHIR_TYPES_BASE_PACKAGE + "." 
            + resource.getResourceType()).getConstructor().newInstance();
      } catch (ClassNotFoundException | IllegalAccessException | InstantiationException 
          | IllegalArgumentException | InvocationTargetException | NoSuchMethodException 
          | SecurityException e) {
        throw new TransformationException("Unable to create resource " + resource.getResourceType(), e);
      }
      fhirResource = (DomainResource) instance;
      fhirResource.setId(fhirId);
      fhirResourceMap.put(fhirId, fhirResource);
    }
    for (AttributeValue attVal : resource.getResourceAttributeValues()) {
      setValue(fhirResource, attVal.getAttributes(), attVal.getValue(), data, recordId);
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
   * @param row The row of data to be used to set this value.
   * @param recordId The id of this record. Used to create the FHIR ids.
   */
  private void setValue(DomainResource resource, List<Attribute> attributes, Value value, Map<String, String> row,
                        String recordId) {

    // Get chain of attribute names
    final List<Attribute> attributesCopy = new ArrayList<>(attributes);
    final Attribute leafAttribute = attributesCopy.remove(attributes.size() - 1);
    final String leafAttributeName = leafAttribute.getName();
    final Integer index = leafAttribute.getAttributeIndex();

    try {
      // Now we need to find or create the object where the value is going to be set
      final Base theElement = helper.getElementToSet(resource, attributesCopy);

      // Now we need to get the value to set
      Base theValue;
      final Field f = helper.getField(theElement.getClass(), leafAttributeName);
    
      // We use the generated annotations in the FHIR model to get the type
      final Child hapiMetadata = f.getAnnotation(Child.class);
      final boolean isValueX = isValueX(hapiMetadata, theElement.getClass());
      final Class<?> fhirType = getTypeFromHapiAnnotations(hapiMetadata, f, leafAttributeName);
      
      // Special case for codes
      Class<?> enumFactory = null;
      if (fhirType != null && (fhirType.equals(CodeType.class) || fhirType.equals(Enumeration.class))) {
        Class<?> hapiType = helper.getParametrisedType(f);
        
        // Now we need the EnumFactory for this type
        if (hapiType != null) {
          log.debug("Getting EnumFactory for type " + hapiType.getName());
          try {
            enumFactory = Class.forName(hapiType.getName() + "EnumFactory");
          } catch (ClassNotFoundException e) {
            throw new TransformationException("Unable to get EnumFactory for class " + hapiType);
          }
        } else {
          log.debug("Code does not have an EnumFactory.");
        }
      }

      theValue = getValue(value, fhirType, row, recordId, enumFactory);
      if (theValue != null) {
        helper.invokeSetter(theElement, leafAttributeName, theValue, leafAttribute.isList(), index, isValueX);
      }
    } catch (TransformationException e) {
      throw e;
    } catch (Exception e) {
      handleReflectionException(e);
    }
  }

  /**
   * Resolves a value.
   *
   * @param value The value specified in the transformation rules.
   * @param fhirType The type of the FHIR attribute where this value will be set.
   * @param row The row of data from REDCap.
   * @param recordId The id of this record. Used to create the references to FHIR ids.
   * @param enumFactory If the type is an enumeration, this is the factory to create an instance.
   * @return The value or null if the value cannot be determined. This can also be a list.
   */
  private Base getValue(Value value, Class<?> fhirType, Map<String, String> row, String recordId,
                        Class<?> enumFactory) {
    // If this is a field-based value then make sure that there is a value and if not return null
    if (value instanceof FieldBasedValue) {
      FieldBasedValue fbv = (FieldBasedValue) value;

      // Account for field ids of the form xx___y
      String fieldId = fbv.getFieldId();
      String shortFieldId = null;

      String regex = "(?<fieldId>.*)___\\d+$";
      Pattern pattern = Pattern.compile(regex);
      Matcher matcher = pattern.matcher(fieldId);
      if (matcher.find()) {
        shortFieldId = matcher.group("fieldId");
        log.debug("Transformed fieldId into '" + fieldId + "'");
      }

      boolean hasValue = false;
      String rawValue = row.get(fieldId);
      if (rawValue != null && !rawValue.isEmpty()) {
        hasValue = true;
      } else if (shortFieldId != null) {
        rawValue = row.get(shortFieldId);
        if (rawValue != null && !rawValue.isEmpty()) {
          hasValue = true;
        }
      }
      if (!hasValue) {
        return null;
      }
    }

    if(value instanceof BooleanValue) {
      return new BooleanType(((BooleanValue) value).getValue());
    } else if (value instanceof CodeLiteralValue) {
      String code = ((CodeLiteralValue) value).getCode();
      return getCode(code, enumFactory);
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
        throw new TransformationException("Got StringValue for FHIR type " + fhirType.getName()
          + ". This should not happen!");
      }
    } else if (value instanceof CodeSelectedValue) {
      CodeSelectedValue csv = (CodeSelectedValue) value;
      String fieldId = csv.getFieldId();
      Mapping m = getSelectedMapping(fieldId, row);
      if (m == null) {
        throw new TransformationException("Mapping for field " + fieldId + " is required but was not found.");
      }
      return getTarget(m).getCodeElement();
    } else if (value instanceof ConceptSelectedValue) {
      ConceptSelectedValue csv = (ConceptSelectedValue) value;
      String fieldId = csv.getFieldId();
      Mapping m = getSelectedMapping(fieldId, row);
      if (m == null) {
        throw new TransformationException("Mapping for field " + fieldId + " is required but was not found.");
      }

      if(fhirType.isAssignableFrom(Coding.class)) {
        return getTarget(m);
      } else if (fhirType.isAssignableFrom(CodeableConcept.class)) {
        return new CodeableConcept().addCoding(getTarget(m));
      } else {
        throw new TransformationException("FHIR type of field " + fieldId + " (" + fhirType
          + ") is incompatible with CONCEPT_SELECTED.");
      }
    } else if (value instanceof ConceptValue) {
      // Ontoserver REDCap plugin format: 74400008|Appendicitis|http://snomed.info/sct
      ConceptValue cv = (ConceptValue) value;
      String fieldId = cv.getFieldId();

      Mapping m = getMapping(fieldId);
      if (m != null) {
        if(fhirType.isAssignableFrom(Coding.class)) {
          return getTarget(m);
        } else if (fhirType.isAssignableFrom(CodeableConcept.class)) {
          return new CodeableConcept().addCoding(getTarget(m));
        } else {
          throw new TransformationException("FHIR type of field " + fieldId + " (" + fhirType
            + ") is incompatible with CONCEPT.");
        }
      } else {
        au.csiro.redmatch.model.Field field = doc.getSchema().getField(fieldId);
        Coding c = field.getCoding(row);
        if (c != null) {
          if(fhirType.isAssignableFrom(Coding.class)) {
            return c;
          } else if (fhirType.isAssignableFrom(CodeableConcept.class)) {
            return new CodeableConcept().addCoding(c);
          } else {
            throw new TransformationException("FHIR type of field " + fieldId + " (" + fhirType
              + ") is incompatible with CONCEPT.");
          }
        }
      }
      throw new TransformationException("Could not get concept for field " + fieldId + ".");
    } else if (value instanceof FieldValue) {
      FieldValue fv = (FieldValue) value;
      String fieldId = fv.getFieldId();
      FieldValue.DatePrecision pr = fv.getDatePrecision();
      au.csiro.redmatch.model.Field field = doc.getSchema().getField(fieldId);
      return field.getValue(row, fhirType, pr);
    } else {
      throw new TransformationException("Unable to get VALUE for " + value);
    }
  }

  private Coding getTarget(Mapping m) {
    Coding coding = m.getTarget();
    if (coding != null) {
      return coding;
    } else {
      throw new TransformationException("Mapping " + m + "does not have a target.");
    }
  }

  /**
   * Removes any empty attributes that might have been created because of the rules.
   * 
   * @param base The resource to prune.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void prune(Base base) {
    log.trace("Pruning " + base);
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
        throw new TransformationException("There was a reflection issue while pruning resources.", e);
      }
    }
    log.trace("The following attributes are set: " + setAttrs);

    // Get set attributes with multiplicity > 1
    final Set<String> multSetAttrs = new HashSet<>();
    for (Method m : c.getMethods()) {
      if (m.getName().startsWith("add") && setAttrs.contains(m.getName().substring(3))) {
        multSetAttrs.add(m.getName().substring(3));
      }
    }
    log.trace("The following attributes with multiplicity > 1 are set: " + multSetAttrs);

    // Prune those attributes
    log.trace("Pruning attributes with multiplicity > 1");
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
        throw new TransformationException("There was a reflection issue while pruning resources.", e);
      }
    }

    // Prune recursively - call the method for every attribute that is set and is not a primitive
    // type
    log.trace("Pruning recursively");
    for (String attr : setAttrs) {
      try {
        log.trace("Invoking method get" + attr + " on " + base);
        Object o = c.getMethod("get" + attr).invoke(base);
        log.trace("Got object of type " + o.getClass());
        if (o instanceof Base) {
          Base b = (Base) o;
          if (!helper.isPrimitive(b.getClass())) {
            prune((Base) o);
          }
        } else if (o instanceof List) {
          for (Object oo : (List) o) {
            if (oo instanceof Base) {
              prune((Base) oo);
            }
          }
        }
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e) {
        throw new TransformationException("There was a reflection issue while pruning resources.", e);
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
  
  private Base getCode(String code, Class<?> enumFactory) {

    if (enumFactory != null) {
      try {
        // Create instance of factory
        Object factory = enumFactory.getDeclaredConstructor().newInstance();

        // Call the fromCode method
        Method fromCode = findMethodByName(enumFactory);
        assert fromCode != null;
        return (Base) fromCode.invoke(factory, new StringType(code));
      } catch (Exception e) {
        handleReflectionException(e);
        return null;
      }
    } else {
      return new CodeType (code);
    }
  }
  
  private Base getConcept(String system, String code, String display, Class<?> type) {
    if (!FitbitUrlValidator.isValid(system)) {
      throw new TransformationException("The system '" + system + "' is invalid.");
    }

    if (!codePattern.matcher(code).matches()) {
      throw new TransformationException("The code " + code + " is invalid.");
    }
    
    if (type.isAssignableFrom(Coding.class)) {
      return new Coding().setSystem(system).setCode(code).setDisplay(display);
    } else if (type.isAssignableFrom(CodeableConcept.class)) {
      return new CodeableConcept().addCoding(
          new Coding().setSystem(system).setCode(code).setDisplay(display));
    } else {
      throw new TransformationException("Expected a Coding or a CodeableConcept but got " + type.getCanonicalName());
    }
  }
  
  /**
   * Gets the mapping for a selected value, accounting for the differences between RADIOs, DROPDOWNs
   * and CHECKBOX_OPTIONs.
   * 
   * @param fieldId The REDCap field id.
   * @param row A row of REDCap data.
   * @return The mapping.
   */
  private Mapping getSelectedMapping(@NotNull String fieldId, @NotNull Map<String, String> row) {
    au.csiro.redmatch.model.Field f = this.doc.getSchema().getField(fieldId);
    return f.findSelectedMapping(doc.getMappings(), row);
  }

  private Mapping getMapping(@NotNull String fieldId) {
    au.csiro.redmatch.model.Field f = this.doc.getSchema().getField(fieldId);
    return f.findMapping(doc.getMappings());
  }
  
  private Method findMethodByName(Class<?> clazz) {
    for (Method method : clazz.getMethods()) {
      if (method.getName().equalsIgnoreCase("fromType")) {
        return method;
      }
    }
    return null;
  }
  
  private void handleReflectionException(Exception e) {
    if (e instanceof NoSuchMethodException) {
      throw new TransformationException("A method could not be found: " + e.getLocalizedMessage(), e);
    } else if (e instanceof NoSuchFieldException) {
      log.error(e);
      throw new TransformationException("A field could not be found: " + e.getLocalizedMessage(), e);
    } else if (e instanceof ClassNotFoundException) {
      throw new TransformationException("A class could not be found: " + e.getLocalizedMessage(), e);
    } else if (e instanceof IllegalAccessException) {
      throw new TransformationException("There was a problem creating a field: " + e.getLocalizedMessage(), e);
    } else if (e instanceof IllegalArgumentException) {
      throw new TransformationException("An illegal argument was used: " + e.getLocalizedMessage(), e);
    } else if (e instanceof InvocationTargetException) {
      InvocationTargetException ite = (InvocationTargetException) e;
      Throwable cause = ite.getCause();
      throw new TransformationException("There was a problem invoking a method or constructor: " +
            (cause != null ? cause.getLocalizedMessage() : e.getLocalizedMessage()), e);
    } else {
      throw new TransformationException ("There was a problem using reflection.", e);
    }
  }
}
