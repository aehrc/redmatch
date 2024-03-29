/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.exporter;

import au.csiro.redmatch.compiler.Attribute;
import au.csiro.redmatch.model.VersionedFhirPackage;
import au.csiro.redmatch.terminology.CodeInfo;
import au.csiro.redmatch.terminology.TerminologyService;
import au.csiro.redmatch.util.FhirUtils;
import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Enumeration;

import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.*;
import java.util.*;

/**
 * Helper class to do reflection with the FHIR HAPI API.
 *
 * @author Alejandro Metke Jimenez
 *
 */
public class HapiReflectionHelper {
  
  public static final String FHIR_TYPES_BASE_PACKAGE = "org.hl7.fhir.r4.model";
  
  private final List<String> fhirComplexTypes = new ArrayList<>();

  private final List<String> fhirBasicTypes = new ArrayList<>();
  
  private final Set<String> reservedWords = new HashSet<>(Arrays.asList("abstract", "assert",
      "boolean", "break", "byte", "case", "catch", "char",  "class", "const", "continue", 
      "default", "double",  "do", "else",  "enum", "extends", "false", "final", "finally", 
      "float", "for", "goto", "if", "implements", "import",  "instanceof", "int", "interface", 
      "long", "native",  "new", "null", "package", "private", "protected", "public", "return",  
      "short", "static", "strictfp", "super", "switch",  "synchronized", "this", "throw", 
      "throws", "transient", "true", "try", "var", "void", "volatile", "while"));

  private final FhirContext ctx;

  private final VersionedFhirPackage defaultFhirPackage;

  private final TerminologyService terminologyService;
  
  /**
   * Sets the FHIR context.
   * 
   * @param ctx The FHIR context.
   */
  public HapiReflectionHelper(FhirContext ctx, VersionedFhirPackage defaultFhirPackage,
                              TerminologyService terminologyService) {
    this.ctx = ctx;
    this.defaultFhirPackage = defaultFhirPackage;
    this.terminologyService = terminologyService;
    init();
  }

  /**
   * Configure restful client.
   */
  private void init() {
    // Load FHIR simple and complex types - needed to get attribute types
    try {
      for (StructureDefinition structureDefinition : FhirUtils.getStructureDefinitions(ctx, defaultFhirPackage)) {
        StructureDefinition.StructureDefinitionKind sdk = structureDefinition.getKind();
        switch (sdk) {
          case COMPLEXTYPE:
            fhirComplexTypes.add(structureDefinition.getType());
            break;

          case PRIMITIVETYPE:
            final String type = structureDefinition.getType();
            fhirBasicTypes.add(Character.toUpperCase(type.charAt(0)) + type.substring(1));
            break;
          case LOGICAL:
          case NULL:
          case RESOURCE:
          default:
            break;
        }
      }

      Collections.sort(fhirBasicTypes, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          if (o1.length() > o2.length()) {
            return -1;
          } else if (o1.length() < o2.length()) {
            return 1;
          } else {
            return o1.compareTo(o2);
          }
        }
      });
      Collections.sort(fhirComplexTypes, new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
          if (o1.length() > o2.length()) {
            return -1;
          } else if (o1.length() < o2.length()) {
            return 1;
          } else {
            return o1.compareTo(o2);
          }
        }
      });
    } catch (IOException e) {
      throw new RuntimeException("There was a problem loading FHIR basic types.", e);
    }
  }

  /**
   * Finds or creates the element where we are going to set a value.
   * 
   * @param resource The resource that contains the element.
   * @param attributes The list of attributes that point at the element where a value is going to be set.
   * @param fhirPackage The FHIR package specified in the rules document. Needed to replace extension names.
   * @param originalResourceType The resource type in the rules. This can be a profile name, so it can be different from
   *    *                        the actual FHIR resource type.
   * @return The element.
   */
  public Base getElementToSet(DomainResource resource, List<Attribute> attributes, VersionedFhirPackage fhirPackage,
                              String originalResourceType)
    throws NoSuchFieldException, NoSuchMethodException, ClassNotFoundException, IllegalAccessException,
    IllegalArgumentException, InvocationTargetException, IOException {

    // Used to get information about the attributes from the terminology service
    StringBuilder sb = new StringBuilder();
    sb.append(originalResourceType);

    Base theElement = resource;
    for (Attribute att : attributes) {
      final String attName = att.getName();
      final boolean isList = att.isList();

      // Check the current attribute path to see if it is an extension - we need to treat extensions differently
      sb.append(".");
      sb.append(attName);
      CodeInfo codeInfo = terminologyService.lookup(fhirPackage, sb.toString());
      String extensionUrl = codeInfo.getExtensionUrl();
      if (extensionUrl != null) {
        // This is a profiled extension, so we set the url attribute based on the information in the profile
        List<?> list = (List<?>) invokeGetter(theElement, "extension");
        if (list.size() > 0) {
          theElement = (Base) list.get(0);
        } else {
          theElement = invokeAdder(theElement, "extension");
        }
        ((Extension) theElement).setUrl(extensionUrl);
      } else {
        if (!isList) {
          // If attribute is not a list then we just need to get it because HAPI auto-creates instances
          theElement = (Base) invokeGetter(theElement, attName);
        } else {
          // If the attribute is a list then we need to do the following:
          // - If the attribute does not have an index, then we either return the first element or
          //   add an element if the list is empty
          // - If the attribute does have an index and there are enough elements, then return the
          //   right element
          // - If the attribute does have an index but there are not enough elements, then call the
          //   add method until the necessary number of elements are created

          if (!att.hasAttributeIndex()) {
            List<?> list = (List<?>) invokeGetter(theElement, attName);
            if (list.size() > 0) {
              theElement = (Base) list.get(0);
            } else {
              theElement = invokeAdder(theElement, attName);
            }
          } else {
            // Find the element in the list or create the necessary elements
            int index = att.getAttributeIndex();

            List<?> list = (List<?>) invokeGetter(theElement, attName);
            int num = index - list.size() + 1;
            if (num > 0) {
              final Method add = getAddMethod(theElement.getClass(), attName);
              Object elem = null;
              for (int j = 0; j < num; j++) {
                elem = add.invoke(theElement);
              }
              theElement = (Base) elem;
            } else {
              theElement = (Base) list.get(index);
            }
          }
        }
      }
    }
    return theElement;
  }
  
  /**
   * Returns the generic attribute name of an attribute of type value[x] or null if it is not of
   * this type.
   * 
   * @param attributeName The specific attribute name, e.g. valueCodeableConcept.
   * @return The generic attribute name (e.g. value) or null if the attribute is not of type
   *         value[x].
   */
  public String getGenericAttributeName(String attributeName) {
    for (String suffix : fhirBasicTypes) {
      if (attributeName.endsWith(suffix)) {
        return attributeName.substring(0, attributeName.length() - suffix.length());
      }
    }

    for (String suffix : fhirComplexTypes) {
      if (attributeName.endsWith(suffix)) {
        return attributeName.substring(0, attributeName.length() - suffix.length());
      }
    }

    return null;
  }
  
  /**
   * Invokes the getter for an attribute of an object.
   * 
   * @param theElement The object.
   * @param attributeName The name of the attribute.
   * @return The result of invoking the getter.
   */
  public Object invokeGetter(Base theElement, String attributeName) 
      throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException, 
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    final Class<? extends Base> theElementClass = theElement.getClass();
    final Method getMethod = getGetMethod(theElementClass, attributeName);

    return getMethod.invoke(theElement);
  }
  
  /**
   * Invokes the add() method for an attribute of an object. Only works for lists (throws a 
   * {@link NoSuchMethodException} if called on attributes with multiplicity x..1).
   */
  public Base invokeAdder(Base theElement, String attributeName) 
      throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException, 
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    final Class<? extends Base> theElementClass = theElement.getClass();
    final Method addMethod = getAddMethod(theElementClass, attributeName);
  
    return (Base) addMethod.invoke(theElement, new Object[0]);
  }
  
  /**
   * Invokes the set() method for an attribute of an object.
   */
  @SuppressWarnings("unchecked")
  public void invokeSetter(Base theElement, String attributeName, Base value, boolean isList, Integer index,
                           boolean isValueX)
    throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
    NoSuchFieldException, ClassNotFoundException {
    final Class<? extends Base> theElementClass = theElement.getClass();
    Class<? extends Base> valueClass = value.getClass();

    if (!isList) {
      // If this is not a list then we just call the set method
      final Method setMethod = getSetMethod(theElementClass, attributeName, valueClass, false, isValueX);
      setMethod.invoke(theElement, value);
    } else {
      // If this is a list and no index is specified, then we call the add method
      if (index == null || index < 0) {
        final Method addMethodWithParam = getAddMethodWithParam(theElementClass, attributeName);
        addMethodWithParam.invoke(theElement, value);
      } else {
        // If an index is specified then we need to get the list and insert the value in the correct
        // position

        List<Base> list = (List<Base>) invokeGetter(theElement, attributeName);
        if (list.size() > index) {
          list.set(index, value);
        } else {
          // Not enough elements in the list so create empty entries
          final Method addMethod = getAddMethod(theElementClass, attributeName);
          int listSize = list.size();
          for (int i = 0; i < index - listSize; i++) {
            addMethod.invoke(theElement);
          }

          // Now add our element
          final Method addMethodWithParam = getAddMethodWithParam(theElementClass, attributeName);
          addMethodWithParam.invoke(theElement, value);
        }
      }
    }
  }

  /**
   * Returns the add method that accepts the element to add as a parameter for an attribute of a
   * given class.
   *
   * @param resourceClass The resource class.
   * @param attributeName The attribute name.
   * @return The corresponding add {@link Method}.
   * @throws ClassNotFoundException If the class that contains the method is not found.
   * @throws NoSuchFieldException If the field is not found.
   * @throws NoSuchMethodException If the method is not found.
   */
  public Method getAddMethodWithParam(Class<? extends Base> resourceClass, String attributeName)
    throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException {
    return findMethod("add", attributeName, 1, resourceClass);
  }
  
  private String handleAttributeName(String att) {
    if (reservedWords.contains(att)) {
      return att + "_";
    } else {
      return att;
    }
  }
  
  /**
   * Looks for a {@link Field} in a FHIR class and its parents.
   * 
   * @param c The {@link Class}.
   * @param att The attribute name.
   * @return The {@link Field}.
   * 
   * @throws NoSuchFieldException If the field cannot be found.
   */
  public Field getField(Class<? extends Base> c, String att) throws NoSuchFieldException {
    
    // Special case: attributes that are named using Java reserved words, e.g. Encounter.class
    att = handleAttributeName(att);
    
    Field f;
    try {
      f = getFieldRecursive(c, att);
    } catch (NoSuchFieldException e) {
      String gatt = getGenericAttributeName(att);
      if (gatt == null) {
        throw new NoSuchFieldException(
            "Could not find attribute " + att + " in class " + c.getName());
      }
      f = getFieldRecursive(c, gatt);
    }
    return f;
  }

  /**
   * Looks for the specified field and if it doesn't find it keeps looking up the class hierachy.
   * 
   * @param type The class.
   * @param name The name of the field.
   * @return The {@link Field} or null if a field with that name cannot be found.
   */
  @SuppressWarnings("unchecked")
  private Field getFieldRecursive(Class<? extends Base> type, String name) throws NoSuchFieldException {
    try {
      return type.getDeclaredField(name);
    } catch (NoSuchFieldException e) {
      if (type.getSuperclass() != null) {
        Class<?> c = type.getSuperclass();
        if (c.equals(Base.class)) {
          throw new NoSuchFieldException("Could not find field " + name + " in class " 
              + type.getName());
        }
        return getFieldRecursive((Class<? extends Base>) c, name);
      } else {
        throw e;
      }
    }
  }
  
  /**
   * Returns the specific attribute type of an attribute of type value[x] or null if it is not of
   * this type.
   * 
   * @param attributeName The specific attribute name, e.g. valueCodeableConcept.
   * @return The class that represents the attribute type 
   * (e.g. org.hl7.fhir.r4.model.CodeableConcept) or null if the attribute is not of type value[x].
   */
  @SuppressWarnings("unchecked")
  public Class<? extends Base> getValueXAttributeType(String attributeName) 
      throws ClassNotFoundException {
    for (String suffix : fhirBasicTypes) {
      if (attributeName.endsWith(suffix)) {
        return (Class<? extends Base>) Class.forName(
            HapiReflectionHelper.FHIR_TYPES_BASE_PACKAGE + "."
            + attributeName.substring(attributeName.length() - suffix.length()) + "Type");
      }
    }

    for (String suffix : fhirComplexTypes) {
      if (attributeName.endsWith(suffix)) {
        return (Class<? extends Base>) Class.forName(
            HapiReflectionHelper.FHIR_TYPES_BASE_PACKAGE + "."
            + attributeName.substring(attributeName.length() - suffix.length()));
      }
    }
    return null;
  }
  
  private boolean isValueXType(String attributeName) {
    for (String suffix : fhirBasicTypes) {
      if (attributeName.endsWith(suffix)) {
        return true;
      }
    }

    for (String suffix : fhirComplexTypes) {
      if (attributeName.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }
  
  private String removeValueX(String attributeName) {
    if (!isValueXType(attributeName)) {
      return attributeName;
    }
    for (String suffix : fhirBasicTypes) {
      if (attributeName.endsWith(suffix)) {
        return attributeName.substring(0, attributeName.length() - suffix.length());
      }
    }

    for (String suffix : fhirComplexTypes) {
      if (attributeName.endsWith(suffix)) {
        return attributeName.substring(0, attributeName.length() - suffix.length());
      }
    }
    throw new RuntimeException("Should never get here!");
  }
  
  /**
   * Returns the add method for an attribute of a given class.
   * 
   * @param resourceClass The resource class.
   * @param attributeName The attribute name.
   * @return The corresponding add {@link Method}.
   */
  public Method getAddMethod(Class<? extends Base> resourceClass, String attributeName) 
      throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException {
    return findMethod("add", attributeName, 0, resourceClass);
  }
  
  /**
   * Returns a method used to set an attribute. Note that in some cases there might be multiple
   * methods with the same name and number of arguments but different parameter types (e.g.
   * {@link Quantity#setValue(java.math.BigDecimal)}, {@link Quantity#setValue(double)} and
   * {@link Quantity#setValue(long)} ).
   * 
   * Setters for primitive types have the suffix Element when using the HAPI types, except when the
   * attribute has multiple types (i.e., value[x]).
   * 
   * @param resourceClass The resource or complex type where the attribute is going to be set.
   * @param attributeName The name of the attribute.
   * @param valueClass The class of the object to set.
   * @param isList Indicates if the attribute is multi-valued.
   * @param isValueX Indicates if the attribute has multiple types (value[x]).
   * @return The set {@link Method}.
   */
  public Method getSetMethod(Class<?extends Base> resourceClass, String attributeName, 
      Class<? extends Base> valueClass, boolean isList, boolean isValueX) 
          throws NoSuchMethodException {
    if (isValueX) {
      attributeName = this.removeValueX(attributeName);
    }
    
    attributeName = handleAttributeName(attributeName);
    
    String methodName = "set" + attributeName 
        + (isPrimitive(valueClass) && !isValueX ? "element" : "");

    if (valueClass != null) {
      for (Method method : resourceClass.getMethods()) {
        if (method.getName().equalsIgnoreCase(methodName) && method.getParameterCount() == 1) {
          Parameter param = method.getParameters()[0];
          Class<?> paramType = param.getType();
          
          if (isList && List.class.isAssignableFrom(paramType)) {
            ParameterizedType pt = (ParameterizedType) param.getParameterizedType();
            Type t = pt.getActualTypeArguments()[0];
            String typeName = t.getTypeName();
            
            if (valueClass.getName().equals(typeName)) {
              return method;
            } else {
              throw new NoSuchMethodException("Found method " + methodName + 
                  " but types do not match: expected = " + valueClass.getName() + ", actual = " 
                  + typeName);
            }
            
          } else if (valueClass.isAssignableFrom(paramType)) {
            return method;
          } else if (isValueX && org.hl7.fhir.r4.model.Type.class.isAssignableFrom(paramType)) {
            return method;
          } else if (valueClass.equals(CodeType.class) 
              && Enumeration.class.isAssignableFrom(paramType)) {
            // Special case for code types that are represented as enumerations in HAPI
            return method;
          }
        }
      }
    }

    throw new NoSuchMethodException("Could not find method " + methodName + " for type "
              + (valueClass != null ? valueClass.getName() : "NA") + " in resource " + resourceClass.getName());
  }
  
  /**
   * Returns a method to get an attribute. When the attribute is a primitive type, the getter that
   * returns the FHIR type, not the Java primitive type, is returned. The same approach is used for
   * Enumerations.
   * 
   * @param resourceClass The resource class.
   * @param attributeName The attribute name.
   * @return The corresponding get {@link Method}.
   */
  public Method getGetMethod(Class<? extends Base> resourceClass, String attributeName) 
      throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException {
    return findMethod("get", attributeName, 0, resourceClass);
  }
  
  /**
   * Find a method in a class.
   * 
   * @param prefix get or has (set is not supported).
   */
  @SuppressWarnings("unchecked")
  private Method findMethod(String prefix, String attributeName, int numParams, Class<? extends Base> resourceClass)
          throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException {

    if (!prefix.equals("get") && !prefix.equals("has") && !prefix.equals("add")) {
      throw new IllegalArgumentException("Parameter 'prefix' should be 'get', 'has' or 'add' but "
        + "was '" + prefix + "'");
    }

    // Need to get the method name - this depends on the attribute
    String methodName = prefix + attributeName;
    Class<?> c = getField(resourceClass, attributeName).getType();
    if (!List.class.isAssignableFrom(c)) {

      // Add methods only exist for lists
      if (prefix.equals("add")) {
        throw new NoSuchMethodException("Method " + methodName + " was not found in class "
          + resourceClass.getName());
      }

      if (isValueXType(attributeName)) {
        // Methods for [x] primitive types need a "Type" suffix
        if (isPrimitive(getValueXAttributeType(attributeName))){
          methodName = methodName + "Type";
        }
      } else if(isPrimitive((Class<? extends Base>) c)) {
        // Methods for primitive types require an "Element" suffix
        methodName = methodName + "Element";
      }
    }

    Method theMethodToCall = null;

    for (Method method : resourceClass.getMethods()) {
      if (method.getName().equalsIgnoreCase(methodName)
        && method.getParameterCount() == numParams) {
        theMethodToCall = method;
        break;
      }
    }

    if (theMethodToCall == null) {
      throw new NoSuchMethodException("Method " + methodName + " was not found in class "
        + resourceClass.getName());
    }

    return theMethodToCall;
  }
  
  /**
   * Returns the parametrised type of a field or null if it does not contain such a type. For
   * example, calling this method on the field <code>List&ltString&gt list;</code> will return the
   * String class.
   * 
   * @param f The field.
   * @return The class of the parametrised type.
   */
  public Class<?> getParametrisedType(Field f) {
    Class<?> parametrisedType = null;

    Type genericType = f.getGenericType();
    if (genericType instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) genericType;
      for (Type t : pt.getActualTypeArguments()) {
        if (t instanceof Class) {
          parametrisedType = (Class<?>) t;
        }
      }
    }

    return parametrisedType;
  }
  
  /**
   * <p>Determines if a class is primitive according to FHIR.</p>
   * 
   * @param c The class.
   * @return True if the class is a primitive according to FHIR.
   */
  public boolean isPrimitive(Class<? extends Base> c) {
    return PrimitiveType.class.isAssignableFrom(c);
  }

}
