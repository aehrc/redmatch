/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.util;

/**
 * Miscellaneous string utilities.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class StringUtils {
  
  /**
   * Returns a string with an indication of where an error occurs.
   * 
   * @param text The string.
   * @param line The line where the error happens.
   * @param charPositionInLine The position in the line where the error happens.
   * @return The string indicating where the error happened.
   */
  public static String toStringWithContext(String text, int line, int charPositionInLine) {
    final String[] lines = text.split("\\r?\\n");
    final StringBuilder sb = new StringBuilder();

    int i = 0;
    while (i < line) {
      if (i < lines.length) {
        sb.append(lines[i++]).append("\n");
      } else {
        break;
      }
    }

    for (int j = 0; j < charPositionInLine; j++) {
      sb.append(' ');
    }
    sb.append('^');

    return sb.toString();
  }

}
