/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.exporter;

import au.csiro.redmatch.compiler.*;
import au.csiro.redmatch.compiler.Resource;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.terminology.CodeInfo;
import au.csiro.redmatch.terminology.TerminologyService;
import au.csiro.redmatch.util.FitbitUrlValidator;
import au.csiro.redmatch.util.StringUtils;
import ca.uhn.fhir.model.api.annotation.Child;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for rule visitors. Provides convenience methods that can be used to create resources based on the rules.
 *
 * @author Alejandro Metke-Jimenez
 */
public abstract class BaseVisitor {

  /** Logger. */
  private static final Log log = LogFactory.getLog(BaseVisitor.class);

  private final Pattern codePattern = Pattern.compile("[^\\s]+([\\s]?[^\\s]+)*");

  /**
   * The transformation rules document.
   */
  protected final Document doc;

  /**
   * Set of resource ids that have a single instance (i.e., an instance is not created for every patient).
   */
  protected final Set<String> uniqueIds;

  protected final HapiReflectionHelper hapiReflectionHelper;
  protected final TerminologyService terminologyService;

  public BaseVisitor(Document doc, Set<String> uniqueIds, HapiReflectionHelper hapiReflectionHelper,
                     TerminologyService terminologyService) {
    this.doc = doc;
    this.uniqueIds = uniqueIds;
    this.hapiReflectionHelper = hapiReflectionHelper;
    this.terminologyService = terminologyService;
  }

  /**
   * Creates a resource and populates its attributes.
   *
   * @param resource The internal resource representation.
   * @param vertex A vertex with patient data.
   * @param recordId The id of this record. Used to create the FHIR ids.
   */
  protected DomainResource createResource(Resource resource, JsonObject vertex, String recordId,
                                          VersionedFhirPackage fhirPackage) {
    final String resourceId = resource.getResourceId();
    final String fhirId = resourceId + (recordId != null ? ("-" + recordId) : "");

    DomainResource fhirResource;
    String resourceType = resource.getResourceType();
    CodeInfo codeInfo;
    try {
      //VersionedFhirPackage fhirPackage = doc.getFhirPackage() != null ? doc.getFhirPackage() : defaultFhirPackage;
      codeInfo = terminologyService.lookup(fhirPackage, resourceType);
      if (codeInfo.isProfile() && codeInfo.getBaseResource() != null) {
        String baseResource = codeInfo.getBaseResource();
        String[] parts = baseResource.split("[/]");
        resourceType = parts[parts.length - 1];
      }
    } catch (IOException e) {
      throw new TransformationException("Unable to lookup information about resource " + resourceType, e);
    }

    // This can be a profile name, so we need to get the base FHIR resource
    Object instance;
    try {
      instance = Class.forName(HapiReflectionHelper.FHIR_TYPES_BASE_PACKAGE + "." + resourceType)
        .getConstructor().newInstance();
    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException
      | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
      | SecurityException e) {
      throw new TransformationException("Unable to create resource " + resource.getResourceType(), e);
    }
    fhirResource = (DomainResource) instance;
    fhirResource.setId(fhirId);

    if (codeInfo.getProfileUrl() != null) {
      fhirResource.getMeta().addProfile(codeInfo.getProfileUrl());
    }

    for (AttributeValue attVal : resource.getResourceAttributeValues()) {
      setValue(fhirResource, attVal.getAttributes(), attVal.getValue(), vertex, recordId, fhirPackage,
        resource.getResourceType());
    }

    return fhirResource;
  }

  protected void handleReflectionException(Exception e) {
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

  /**
   * Assigns the value to the specified attribute of the resource. Tries to accommodate the types but
   * might fail if the specified value is incompatible with the attribute type.
   *
   * @param resource The FHIR resource where the value is going to be set.
   * @param attributes A list of {@link Attribute}. This represents a single attribute that might be
   *        several levels down. The list represents the path to the attribute.
   * @param value The value to set.
   * @param vertex A vertex with patient data.
   * @param recordId The id of this record. Used to create the FHIR ids.
   * @param originalResourceType The resource type in the rules. This can be a profile name, so it can be different from
   *                             the actual FHIR resource type.
   */
  private void setValue(DomainResource resource, List<Attribute> attributes, Value value, JsonObject vertex,
                        String recordId, VersionedFhirPackage fhirPackage, String originalResourceType) {

    // Get chain of attribute names
    final List<Attribute> attributesCopy = new ArrayList<>(attributes);
    final Attribute leafAttribute = attributesCopy.remove(attributes.size() - 1);
    final String leafAttributeName = leafAttribute.getName();
    final Integer index = leafAttribute.getAttributeIndex();

    try {
      // Now we need to find or create the object where the value is going to be set
      final Base theElement = hapiReflectionHelper.getElementToSet(resource, attributesCopy, fhirPackage,
        originalResourceType);

      // Now we need to get the value to set
      Base theValue;
      final java.lang.reflect.Field f = hapiReflectionHelper.getField(theElement.getClass(), leafAttributeName);

      // We use the generated annotations in the FHIR model to get the type
      final Child hapiMetadata = f.getAnnotation(Child.class);
      final boolean isValueX = isValueX(hapiMetadata, theElement.getClass());
      final Class<?> fhirType = getTypeFromHapiAnnotations(hapiMetadata, f, leafAttributeName);

      // Special case for codes
      Class<?> enumFactory = null;
      if (fhirType != null && (fhirType.equals(CodeType.class) || fhirType.equals(Enumeration.class))) {
        Class<?> hapiType = hapiReflectionHelper.getParametrisedType(f);

        // Now we need the EnumFactory for this type
        if (hapiType != null) {
          try {
            enumFactory = Class.forName(hapiType.getName() + "EnumFactory");
          } catch (ClassNotFoundException e) {
            throw new TransformationException("Unable to get EnumFactory for class " + hapiType);
          }
        } else {
          log.debug("Code does not have an EnumFactory.");
        }
      }

      theValue = getValue(value, fhirType, vertex, recordId, enumFactory, fhirPackage);
      if (theValue != null) {
        hapiReflectionHelper.invokeSetter(theElement, leafAttributeName, theValue, leafAttribute.isList(), index,
          isValueX);
      }
    } catch (TransformationException e) {
      throw e;
    } catch (Exception e) {
      handleReflectionException(e);
    }
  }

  private Class<?> getTypeFromHapiAnnotations(final Child hapiMetadata, Field f, String leafAttributeName)
    throws ClassNotFoundException {
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
      return hapiReflectionHelper.getValueXAttributeType(leafAttributeName);
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
   * Resolves a value.
   *
   * @param value The value specified in the transformation rules.
   * @param fhirType The type of the FHIR attribute where this value will be set.
   * @param vertex A vertex with patient data.
   * @param recordId The id of this record. Used to create the references to FHIR ids.
   * @param enumFactory If the type is an enumeration, this is the factory to create an instance.
   * @param fhirPackage The target FHIR package.
   * @return The value or null if the value cannot be determined. This can also be a list.
   */
  private Base getValue(Value value, Class<?> fhirType, JsonObject vertex, String recordId, Class<?> enumFactory,
                        VersionedFhirPackage fhirPackage) throws IOException {
    // If this is a field-based value then make sure that there is a value and if not return null
    if (value instanceof FieldBasedValue) {
      assert vertex != null && recordId != null;
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
      JsonElement jsonElement = vertex.get(fieldId);
      if (jsonElement != null) {
        String rawValue = jsonElement.getAsString();
        if (!rawValue.isEmpty()) {
          hasValue = true;
        }
      }

      if (!hasValue && shortFieldId != null) {
        jsonElement = vertex.get(shortFieldId);
        if (jsonElement != null) {
          String rawValue = jsonElement.getAsString();
          if (!rawValue.isEmpty()) {
            hasValue = true;
          }
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
      boolean unique = uniqueIds.contains(resourceType + "<" + resourceId + ">");

      CodeInfo codeInfo = terminologyService.lookup(fhirPackage, resourceType);
      if (codeInfo.isProfile()) {
        resourceType = StringUtils.getLastPath(codeInfo.getBaseResource());
      }

      if (unique) {
        // This is a reference to a unique resource - no need to append row id
        ref.setReference("/" + resourceType + "/" + resourceId);
      } else {
        ref.setReference("/" + resourceType + "/" + resourceId + "-" + recordId);
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
      Mapping m = getSelectedMapping(fieldId, vertex);
      if (m == null) {
        throw new TransformationException("Mapping for field " + fieldId + " is required but was not found.");
      }
      return getTarget(m).getCodeElement();
    } else if (value instanceof ConceptSelectedValue) {
      ConceptSelectedValue csv = (ConceptSelectedValue) value;
      String fieldId = csv.getFieldId();
      Mapping m = getSelectedMapping(fieldId, vertex);
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
        Coding c = field.getCoding(vertex);
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
      return field.getValue(vertex, fhirType, pr);
    } else {
      throw new TransformationException("Unable to get VALUE for " + value);
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

  private Method findMethodByName(Class<?> clazz) {
    for (Method method : clazz.getMethods()) {
      if (method.getName().equalsIgnoreCase("fromType")) {
        return method;
      }
    }
    return null;
  }

  private Coding getTarget(Mapping m) {
    Coding coding = m.getTarget();
    if (coding != null) {
      return coding;
    } else {
      throw new TransformationException("Mapping " + m + "does not have a target.");
    }
  }

  private Mapping getMapping(@NotNull String fieldId) {
    au.csiro.redmatch.model.Field f = this.doc.getSchema().getField(fieldId);
    return f.findMapping(doc.getMappings());
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
   * @param vertex A vertex with patient data.
   * @return The mapping.
   */
  private Mapping getSelectedMapping(@NotNull String fieldId, @NotNull JsonObject vertex) {
    au.csiro.redmatch.model.Field f = this.doc.getSchema().getField(fieldId);
    return f.findSelectedMapping(doc.getMappings(), vertex);
  }

}
