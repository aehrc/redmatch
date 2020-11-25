/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
 */
package au.csiro.redmatch.exporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.BackboneElement;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Enumeration;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.csiro.redmatch.model.grammar.redmatch.Attribute;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.parser.JsonParser;

/**
 * @author Alejandro Metke
 *
 */
@Component
public class HapiReflectionHelper {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(HapiReflectionHelper.class);
  
  public static final String FHIR_TYPES_BASE_PACKAGE = "org.hl7.fhir.r4.model";
  
  private final List<String> fhirComplexTypes = new ArrayList<>();

  private final List<String> fhirBasicTypes = new ArrayList<>();
  
  private final Set<String> reservedWords = new HashSet<String>(Arrays.asList("abstract", "assert", 
      "boolean", "break", "byte", "case", "catch", "char",  "class", "const", "continue", 
      "default", "double",  "do", "else",  "enum", "extends", "false", "final", "finally", 
      "float", "for", "goto", "if", "implements", "import",  "instanceof", "int", "interface", 
      "long", "native",  "new", "null", "package", "private", "protected", "public", "return",  
      "short", "static", "strictfp", "super", "switch",  "synchronized", "this", "throw", 
      "throws", "transient", "true", "try", "var", "void", "volatile", "while"));
  
  @Autowired
  private FhirContext ctx;
  
  /**
   * Sets the FHIR context.
   * 
   * @param ctx The FHIR context.
   */
  public void setCtx(FhirContext ctx) {
    this.ctx = ctx;
  }

  /**
   * Configure restful client.
   */
  @PostConstruct
  public void init() {
    // Load FHIR simple and complex types - needed to get attribute types
    try (final InputStream in = FhirExporter.class.getClassLoader()
        .getResourceAsStream("fhir-metadata/4.0.1/profiles-types.json")) {

      if (in == null) {
        throw new RuntimeException("Could not load profiles-types.json");
      }

      final JsonParser parser = (JsonParser) ctx.newJsonParser();
      final Bundle types = parser.doParseResource(Bundle.class,
          new InputStreamReader(in, Charset.forName("UTF8")));

      for (BundleEntryComponent bec : types.getEntry()) {
        org.hl7.fhir.r4.model.Resource r = bec.getResource();
        if (r instanceof StructureDefinition) {
          StructureDefinition sd = (StructureDefinition) r;
          org.hl7.fhir.r4.model.StructureDefinition.StructureDefinitionKind sdk = sd.getKind();
          switch (sdk) {
            case COMPLEXTYPE:
              fhirComplexTypes.add(sd.getType());
              break;
            case LOGICAL:
              break;
            case NULL:
              break;
            case PRIMITIVETYPE:
              final String type = sd.getType();
              fhirBasicTypes.add(Character.toUpperCase(type.charAt(0)) + type.substring(1));
              break;
            case RESOURCE:
              break;
            default:
              break;
          }
        }
      }

      log.info("Loaded the following FHIR primitive types: " + fhirBasicTypes);
      log.info("Loaded the following FHIR complex types: " + fhirComplexTypes);

    } catch (IOException e) {
      throw new RuntimeException("There was a problem loading profiles-types.json", e);
    }
  }
  
  /**
   * Finds or creates the element where we are going to set a value.
   * 
   * @param resource The resource that contains the element.
   * @param attributes The list of attributes that point at the element where a value is going to be
   *        set.
   * @return The element.
   * @throws NoSuchFieldException 
   * @throws NoSuchMethodException 
   * @throws ClassNotFoundException 
   * @throws InvocationTargetException 
   * @throws IllegalArgumentException 
   * @throws IllegalAccessException 
   */
  public Base getElementToSet(DomainResource resource, List<Attribute> attributes) 
      throws NoSuchFieldException, NoSuchMethodException, ClassNotFoundException, 
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Base theElement = resource;
    for (Attribute att : attributes) {
      final String attName = att.getName();
      final boolean isList = att.isList();
      
      if (!isList) {
        // If attribute is not a list then we just need to get it because HAPI autocreates instances
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
            for (int i = 0; i < num; i++) {
              elem = add.invoke(theElement, new Object[0]);
            }
            theElement = (Base) elem;
          } else {
            theElement = (Base) list.get(index);
          }
        }
      }
    }
    return theElement;
  }
  
  /**
   * Returns true if a FHIR element has an attribute set.
   * 
   * @param theElement The FHIR element.
   * @param attributeName The attribute name.
   * @return True if set, false otherwise.
   * @throws NoSuchFieldException 
   * @throws NoSuchMethodException 
   * @throws ClassNotFoundException 
   * @throws InvocationTargetException 
   * @throws IllegalArgumentException 
   * @throws IllegalAccessException 
   */
  public boolean isAttributeSet(Base theElement, String attributeName) 
      throws NoSuchFieldException, NoSuchMethodException, ClassNotFoundException, 
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    final Class<? extends Base> theElementClass = theElement.getClass();
    final Method hasMethod = getHasMethod(theElementClass, attributeName);
    return ((Boolean) hasMethod.invoke(theElement, new Object[0])).booleanValue();
  }
  
  /**
   * TODO: do we need this?
   * 
   * Returns the generic attribute name for an element.
   * 
   * @param theElement The element.
   * @param attributeName The attribute name.
   * @return The generic attribute name.
   * @throws NoSuchFieldException 
   */
  String getGenericAttributeName(Base theElement, String attributeName) 
      throws NoSuchFieldException {
    String res = attributeName;
    final Field field = getField(theElement.getClass(), attributeName);
    final Class<?>[] types = getHapiTypes(theElement.getClass(), attributeName);
    final Class<?> declaredType = getDeclaredType(theElement.getClass(), attributeName);

    if (types.length == 0) {
      // Two options here: the element is a backbone element, in which case the attribute name does
      // not need to change, or the element is a Type element with no constraints 
      // (e.g. Extension.value) in which case the attribute name does need to change
      if (List.class.isAssignableFrom(declaredType)) {
        Class<?> c = getParametrisedType(field);
        if (!BackboneElement.class.isAssignableFrom(c)) {
          res = getGenericAttributeName(attributeName);
        }
      } else {
        if (!BackboneElement.class.isAssignableFrom(declaredType)) {
          res = getGenericAttributeName(attributeName);
        }
      }
    } else if (types.length == 1) {
      // No need to change the name of the attribute
    } else {
      // Need to change the name of the attribute unless this is a reference, because reference is
      // special - more than one type does not imply a value[x]
      // TODO: Can there be a list of references?
      if (!declaredType.getName().equals(HapiReflectionHelper.FHIR_TYPES_BASE_PACKAGE 
          + ".Reference")) {
        res = getGenericAttributeName(attributeName);
      }
    }

    return res;
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
   * @throws NoSuchMethodException 
   * @throws NoSuchFieldException 
   * @throws ClassNotFoundException 
   * @throws InvocationTargetException 
   * @throws IllegalArgumentException 
   * @throws IllegalAccessException 
   */
  public Object invokeGetter(Base theElement, String attributeName) 
      throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException, 
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    final Class<? extends Base> theElementClass = theElement.getClass();
    final Method getMethod = getGetMethod(theElementClass, attributeName);

    return getMethod.invoke(theElement, new Object[0]);
  }
  
  /**
   * Invokes the add() method for an attribute of an object. Only works for lists (throws a 
   * {@link NoSuchMethodException} if called on attributes with multiplicity x..1).
   * 
   * @param theElement
   * @param attributeName
   * @return
   * @throws NoSuchMethodException
   * @throws NoSuchFieldException
   * @throws ClassNotFoundException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
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
   * 
   * @param theElement
   * @param attributeName
   * @param valueClass
   * @param isList
   * @param index
   * @param isValueX
   * 
   * @return
   * @throws NoSuchMethodException
   * @throws NoSuchFieldException
   * @throws ClassNotFoundException
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   * @throws InvocationTargetException
   */
  @SuppressWarnings("unchecked")
  public Base invokeSetter(Base theElement, String attributeName, Base value, boolean isList, 
      Integer index, boolean isValueX) throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException, 
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    final Class<? extends Base> theElementClass = theElement.getClass();
    Class<? extends Base> valueClass = value.getClass();
    
    if (!isList) {
      // If this is not a list then we just call the set method
      final Method setMethod = getSetMethod(theElementClass, attributeName, valueClass, isList, 
          isValueX);
      return (Base) setMethod.invoke(theElement, new Object[] { value });
    } else {
      // If this is a list and no index is specified, then we call the add method
      if (index == null || index < 0) {
        final Method addMethodWithParam = getAddMethodWithParam(theElementClass, attributeName);
        return (Base) addMethodWithParam.invoke(theElement, new Object[] { value });
      } else {
        // If an index is specified then we need to get the list and insert the value in the correct
        // position
        
        List<Base> list = (List<Base>) invokeGetter(theElement, attributeName);
        if (list.size() > index) {
          list.set(index.intValue(), value);
          return theElement;
        } else {
          // Not enough elements in the list so create empty entries
          final Method addMethod = getAddMethod(theElementClass, attributeName);
          int listSize = list.size();
          for (int i = 0; i < index - listSize; i++) {
            addMethod.invoke(theElement, new Object[] { });
          }
          
          // Now add our element
          final Method addMethodWithParam = getAddMethodWithParam(theElementClass, attributeName);
          return (Base) addMethodWithParam.invoke(theElement, new Object[] { value });
        }
      }
    }
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
   * @throws NoSuchFieldException 
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
   * @throws ClassNotFoundException 
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
   * @throws ClassNotFoundException 
   * @throws NoSuchFieldException 
   * @throws NoSuchMethodException 
   */
  public Method getAddMethod(Class<? extends Base> resourceClass, String attributeName) 
      throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException {
    return findMethod("add", attributeName, 0, resourceClass);
  }
  
  /**
   * Returns the add method that accepts the element to add as a parameter for an attribute of a 
   * given class.
   * 
   * @param resourceClass The resource class.
   * @param attributeName The attribute name.
   * @return The corresponding add {@link Method}.
   * @throws ClassNotFoundException 
   * @throws NoSuchFieldException 
   * @throws NoSuchMethodException 
   */
  public Method getAddMethodWithParam(Class<? extends Base> resourceClass, String attributeName) 
      throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException {
    return findMethod("add", attributeName, 1, resourceClass);
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
   * @throws NoSuchMethodException 
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
              + valueClass.getName() + " in resource " + resourceClass.getName());
  }
  
  /**
   * Returns a method to get an attribute. When the attribute is a primitive type, the getter that
   * returns the FHIR type, not the Java primitive type, is returned. The same approach is used for
   * Enumerations.
   * 
   * @param resourceClass The resource class.
   * @param attributeName The attribute name.
   * @return The corresponding get {@link Method}.
   * @throws NoSuchMethodException 
   * @throws ClassNotFoundException 
   * @throws NoSuchFieldException 
   */
  public Method getGetMethod(Class<? extends Base> resourceClass, String attributeName) 
      throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException {
    return findMethod("get", attributeName, 0, resourceClass);
  }
  
  /**
   * Returns a method to determine if an object has an attribute set. There are specific has methods
   * for attributes of type value[x].
   * 
   * @param resourceClass The resource class.
   * @param attributeName The attribute name.
   * @return The corresponding has {@link Method}.
   * @throws NoSuchMethodException 
   * @throws ClassNotFoundException 
   * @throws NoSuchFieldException 
   */
  public Method getHasMethod(Class<? extends Base> resourceClass, String attributeName) 
      throws NoSuchMethodException, NoSuchFieldException, ClassNotFoundException {
    return findMethod("has", attributeName, 0, resourceClass);
  }
  
  /**
   * Find a method in a class.
   * 
   * @param prefix get or has (set is not supported).
   * @param attributeName
   * @param numParams
   * @param resourceClass
   * @return
   * @throws NoSuchMethodException
   * @throws NoSuchFieldException 
   * @throws ClassNotFoundException 
   */
  @SuppressWarnings("unchecked")
  private Method findMethod(String prefix, String attributeName, int numParams, 
      Class<? extends Base> resourceClass) 
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
   * Returns the class(es) of an attribute of a FHIR class using the annotations in HAPI.
   * 
   * @param c The FHIR class.
   * @param att The name of the attribute. If this is a value[x]-type attribute then this will be
   *        the specific name, e.g. valueCodeableConcept.
   * @return An array of types.
   * @throws NoSuchFieldException 
   */
  @SuppressWarnings("unchecked")
  public Class<? extends Base>[] getHapiTypes(Class<? extends Base> c, String att) 
      throws NoSuchFieldException {
    final Field f = getField(c, att);
    final Child ch = f.getAnnotation(Child.class);
    final Class<? extends Base>[] types = (Class<? extends Base>[]) ch.type();
    return types;
  }
  
  /**
   * Returns the declared type of an attribute. Looks also in the parents recursively.
   * 
   * @param c The class.
   * @param att The attribute.
   * @return The declared type. Can be a list.
   * @throws NoSuchFieldException 
   */
  public Class<?> getDeclaredType(Class<? extends Base> c, String att) throws NoSuchFieldException {
    final Field f = getField(c, att);
    return f.getType();
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

    java.lang.reflect.Type genericType = f.getGenericType();
    if (genericType instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) genericType;
      for (java.lang.reflect.Type t : pt.getActualTypeArguments()) {
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
    if (PrimitiveType.class.isAssignableFrom(c)) {
      return true;
    } else {
      return false;
    }
  }

}
