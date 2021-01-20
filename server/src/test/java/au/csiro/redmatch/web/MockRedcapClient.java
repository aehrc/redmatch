/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
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
  
  private String file;
  
  public MockRedcapClient(String file) {
    this.file = file;
  }
  
  @Override
  public String getMetadata(String url, String token, Set<String> fieldIds) throws HttpException {
    return loadMetadataString(file);
  }

  @Override
  public String getReport(String url, String token, String reportId) throws HttpException {
    return loadReportString(file);
  }

}
