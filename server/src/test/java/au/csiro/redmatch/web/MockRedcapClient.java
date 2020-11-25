/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
 */
package au.csiro.redmatch.web;

import java.util.Set;

import au.csiro.redmatch.ResourceLoader;
import au.csiro.redmatch.client.HttpException;
import au.csiro.redmatch.client.IRedcapClient;

/**
 * Mock REDCap client used in testing.
 * 
 * @author Alejandro Metke
 *
 */
public class MockRedcapClient extends ResourceLoader implements IRedcapClient {

  @Override
  public String getMetadata(String url, String token, Set<String> fieldIds) throws HttpException {
    return loadMetadataString("tutorial");
  }

  @Override
  public String getReport(String url, String token, String reportId) throws HttpException {
    return loadReportString("tutorial");
  }

}
