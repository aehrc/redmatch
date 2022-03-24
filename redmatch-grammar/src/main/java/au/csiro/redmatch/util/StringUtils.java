/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import au.csiro.redmatch.model.Field;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * String utilities.
 *
 * @author Alejandro Metke Jimenez
 */
public class StringUtils {

  public static String getClosest(String str, Collection<String> candidates) {
    int min = Integer.MAX_VALUE;
    int index = -1;
    String[] candidatesArray = candidates.toArray(new String[0]);
    for(int i = 0; i < candidatesArray.length; i++) {
      String candidate = candidatesArray[i];
      int distance = editDistance(str, candidate);
      if (distance < min) {
        min = distance;
        index = i;
      }
    }
    return candidatesArray[index];
  }

  public static int editDistance(String x, String y) {
    int[][] dp = new int[x.length() + 1][y.length() + 1];

    for (int i = 0; i <= x.length(); i++) {
      for (int j = 0; j <= y.length(); j++) {
        if (i == 0) {
          dp[i][j] = j;
        }
        else if (j == 0) {
          dp[i][j] = i;
        }
        else {
          dp[i][j] = min(dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
            dp[i - 1][j] + 1,
            dp[i][j - 1] + 1);
        }
      }
    }

    return dp[x.length()][y.length()];
  }

  public static String getLastPath(String url) {
    String[] parts = url.split("[/]");
    return parts[parts.length - 1];
  }

  private static int costOfSubstitution(char a, char b) {
    return a == b ? 0 : 1;
  }

  private static int min(int... numbers) {
    return Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
  }


}
