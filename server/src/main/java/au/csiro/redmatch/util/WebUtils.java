/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Miscellaneous web utilities.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class WebUtils {

  /**
   * Parsers an accept header and returns a set of all accepts, excluding quality.
   * 
   * @param acceptHeader The accept header string.
   * @return A set of all accepts, excluding quality.
   */
  public static Set<String> parseAcceptHeaders(String acceptHeader) {
    final Set<String> res = new HashSet<>();
    final String[] parts = acceptHeader.split("[,]");
    for (String part : parts) {
      final String[] subParts = part.split("[;]");
      res.add(subParts[0].trim());
    }
    return res;
  }

}
