/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.util;

/**
 * Utilities related to identifiers.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class IdUtils {

  /**
   * Returns the id for a mapping of a choice.
   * 
   * @param fieldName The name of the REDCap field.
   * @param redcapCode The code for the choice.
   * @return The id of the choice.
   */
  public static String getChoiceId(String fieldName, String redcapCode) {
    return fieldName + "___" + redcapCode;
  }

  public static String fhiriseId(String id) {
    return id.replaceAll("[^a-zA-Z0-9\\-\\.]", "-");
  }
  
  /**
   * Returns the string in camel case format.
   * 
   * @param string The input string.
   * @return The input string in camel case format.
   */
  public static String toCamelCase(String string) {
    if (string.isEmpty()) {
      return "";
    }
    char[] chars = string.toCharArray();
    char[] res = new char[chars.length];
    res[0] = Character.toUpperCase(chars[0]);
    for (int i = 1; i < chars.length; i++) {
      res[i] = Character.toLowerCase(chars[i]);
    }
    return new String(res);
  }

  public static String toFhirCodeString(String s) {
    return s.trim().replaceAll(" +", " ");
  }

}
