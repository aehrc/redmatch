/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

  /**
   * Returns a string's SHA256 hash.
   *
   * @param s The string.
   * @return It's hash.
   */
  public static String sha256(String s) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encodedhash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(encodedhash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);
    for (int i = 0; i < hash.length; i++) {
      String hex = Integer.toHexString(0xff & hash[i]);
      if(hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

}
