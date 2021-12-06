/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.util;

/**
 * Interface used to report progress from operations.
 *
 * @author Alejandro Metke Jimenez
 */
public interface ProgressReporter {
  void reportProgress (Progress progress);
}
