/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import au.csiro.redmatch.compiler.FieldValue;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Date utilities.
 *
 * @author Alejandro Metke-Jimenez
 *
 */
public class DateUtils {

  public static Date clear(Date date, FieldValue.DatePrecision precision) {
    Calendar instance = Calendar.getInstance();
    instance.setTime(date);

    switch (precision) {
      case YEAR:
        instance.set(Calendar.MONTH, 0);
        instance.set(Calendar.DAY_OF_MONTH, 1);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        break;
      case MONTH:
        instance.set(Calendar.DAY_OF_MONTH, 1);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        break;
      case DAY:
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        break;
    }
    return instance.getTime();
  }

  public static String prettyPrintMillis(long ms) {
    //Time elapsed: 0.461 s
    return String.format("%d.%d s",
      TimeUnit.MILLISECONDS.toSeconds(ms),
      (ms - TimeUnit.MILLISECONDS.toSeconds(ms))
    );
  }

}
