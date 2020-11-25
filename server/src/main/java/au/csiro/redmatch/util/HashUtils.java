/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
 */
package au.csiro.redmatch.util;

/**
 * Hashing utilities.
 * 
 * @author Alejandro Metke
 *
 */
public class HashUtils {
  
  /**
   * Hashes a string into a string of maximum length 5.
   * 
   * @param s The string to hash.
   * @return The hash.
   */
  public static String shortHash(String s) {
    if (s == null) {
      return "0";
    }
    return String.valueOf(Math.abs(s.hashCode() % 100000));
  }

}
