/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import au.csiro.redmatch.model.grammar.redmatch.FieldValue;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for date utilities.
 *
 * @author Alejandro Metke
 *
 */
public class DateUtilsTest {

  // 2001-07-09
  // 2009-01-12
  @Test
  public void testClear() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    SimpleDateFormat sdfExpected = new SimpleDateFormat("yyyy");
    Date date1 = sdf.parse("2009-01-12 05:55");
    Date date1Expected = sdfExpected.parse("2009");
    Date date1Actual = DateUtils.clear(date1, FieldValue.DatePrecision.YEAR);
    assertEquals(date1Expected, date1Actual);
  }

}
