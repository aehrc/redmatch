/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
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
