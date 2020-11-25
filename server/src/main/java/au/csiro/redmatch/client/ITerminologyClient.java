/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
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