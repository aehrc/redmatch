/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Miscellaneous reflection utilities.
 *
 * @author Alejandro Metke-Jimenez
 */
public class ReflectionUtils {

  /** Logger. */
  private static final Log log = LogFactory.getLog(ReflectionUtils.class);

  @SuppressWarnings("rawtypes")
  public static void merge(Object src, Object tgt) throws InvocationTargetException, IllegalAccessException,
    NoSuchMethodException {
    if(!src.getClass().isAssignableFrom(tgt.getClass())){
      log.warn("Attempted to merge two incompatible objects: " + src.getClass() + ", " + tgt.getClass());
      return;
    }

    for(Method hasMethod : src.getClass().getMethods()) {
      if(hasMethod.getReturnType() == boolean.class && hasMethod.getName().startsWith("has")
        && hasMethod.getParameterCount() == 0) {
        // Invoke to see if the value is set
        boolean hasThisAttribute = (boolean) hasMethod.invoke(src);
        if (hasThisAttribute) {

          String getName = hasMethod.getName().replace("has", "get");
          Method getMethod = src.getClass().getMethod(getName);
          Object value = getMethod.invoke(src);
          if (value instanceof List) {
            List list = (List) value;
            // If this is a list them iterate and invoke the add method
            String addName = hasMethod.getName().replace("has", "add");
            try {
              Method addMethod = tgt.getClass().getMethod(addName, list.get(0).getClass());
              for (Object elem : list) {
                addMethod.invoke(tgt, elem);
              }
            } catch (NoSuchMethodException e) {
              // This can happen with value[x]s, e.g.,
              // Observation.setValueCodeableConcept(org.hl7.fhir.r4.model.CodeableConcept) - just ignore
            }
          } else {
            // Otherwise, just invoke the set method
            String setName = hasMethod.getName().replace("has", "set");
            try {
              Method setMethod = tgt.getClass().getMethod(setName, getMethod.getReturnType());
              setMethod.invoke(tgt, value);
            } catch (NoSuchMethodException e) {
              // This can happen with value[x]s - just ignore
            }
          }
        }
      }
    }
  }
}
