/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.csiro.redmatch.exceptions.RedmatchException;

/**
 * Miscellaneous reflection utilities.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class ReflectionUtils {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(ReflectionUtils.class);

  /**
   * Filters the top-level attributes elements in an object by creating a copy with only the 
   * specified attribtues set.
   * 
   * @param clazz The class of the object.
   * @param obj The object to filter.
   * @param elems The list of names of top-level attributes to keep.
   * @return A copy of the elements with only the specified top-level attributes set.
   */
  @SuppressWarnings("unchecked")
  public static <T> T filterElems(Class<T> clazz, Object obj, List<String> elems) {
    if (elems.isEmpty()) {
      return (T) obj;
    }
    
    try {
      final T res = clazz.getDeclaredConstructor().newInstance();
      for (String elem : elems) {
        final String elemCaps = Character.toUpperCase(elem.charAt(0)) + elem.substring(1);

        // Find get method
        Method get = null;
        Method set = null;

        // Find set method - we assume there is only one with one parameter
        for (Method method : clazz.getMethods()) {
          if (method.getName().equals("get" + elemCaps) && method.getParameterCount() == 0) {
            get = method;
          } else if (method.getName().equals("set" + elemCaps) && method.getParameterCount() == 1) {
            set = method;
          }
        }
        if (get == null || set == null) {
          log.warn("Unknown element " + elem + ". Ignoring.");
          continue;
        }
        set.invoke(res, get.invoke(obj));
      }
      return res;
    } catch (IllegalArgumentException | IllegalAccessException | InstantiationException 
        | SecurityException | InvocationTargetException | NoSuchMethodException e) {
      throw new RedmatchException("There was a problem filtering elements using reflection.", e);
    }
  }

}
