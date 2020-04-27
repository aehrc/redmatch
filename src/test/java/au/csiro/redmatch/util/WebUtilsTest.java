/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import au.csiro.redmatch.util.WebUtils;

/**
 * Unit tests for web utilities.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class WebUtilsTest {

  @Test
  public void testParseAcceptHeaders() {
    final Set<String> res = WebUtils
        .parseAcceptHeaders("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
    assertEquals(4, res.size());

    assertTrue(res.contains("text/html"));
    assertTrue(res.contains("application/xhtml+xml"));
    assertTrue(res.contains("application/xml"));
    assertTrue(res.contains("*/*"));
  }

}
