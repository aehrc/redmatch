/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.client;

import java.util.Collection;

import org.hl7.fhir.r4.model.Parameters;

/**
 * An interface that defines the operations required from a terminology server for validation.
 * 
 * @author Alejandro Metke
 *
 */
public interface ITerminologyClient {
  
  /**
   * Validates a code in a code system.
   * 
   * @param system The code system url.
   * @param code The code to validate.
   * @return Parameters object indicating the result of the validation.
   */
  public Parameters validateCode (String system, String code);
  
  /**
   * Looks up a code in a code system.
   * 
   * @param system The code system url.
   * @param code The code to validate.
   * @param attributes The attributes of the code system to return.
   * @return Parameters object with the result of the lookup.
   */
  public Parameters lookup (String system, String code, Collection<String> attributes);
  
}