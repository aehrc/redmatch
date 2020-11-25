/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
 */
package au.csiro.redmatch.client;

import java.util.Set;

/**
 * An interface that defines the operations required from a REDCap client to extract the information
 * needed to populate a Redmatch project.
 * 
 * @author Alejandro Metke
 *
 */
public interface IRedcapClient {
  
  /**
   * Returns the metadata for a REDCap project in JSON format.
   * 
   * @param url The URL of the REDCap installation.
   * @param token The API token.
   * @param fieldIds The fields to request.
   * @return A string with the metadata in JSON format.
   * @throws HttpException If the response is not a 200 code.
   */
  public String getMetadata(String url, String token, Set<String> fieldIds) throws HttpException;
  
  /**
   * Returns the contents of a report in REDCap.
   * 
   * @param url The URL of the REDCap installation.
   * @param token The API token.
   * @param reportId The id of the REDCap report.
   * @return A string with the report's content in JSON format.
   * @throws HttpException If the response is not a 200 code.
   */
  public String getReport(String url, String token, String reportId) throws HttpException;
  
}
