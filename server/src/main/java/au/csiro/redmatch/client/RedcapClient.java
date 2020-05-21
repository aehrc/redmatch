/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use
 * is subject to license terms and conditions.
 */

package au.csiro.redmatch.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import au.csiro.redmatch.exceptions.RedmatchException;

/**
 * Client to communicate with REDCap.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Component
public class RedcapClient {

  /** Logger. */
  private static final Log log = LogFactory.getLog(RedcapClient.class);
  
  /**
   * Returns the project information for a REDCap project in JSON format.
   * 
   * @param url The URL of the REDCap installation.
   * @param token The API token.
   * @return A response with a status and content.
   */
  public String getProjectInfo(String url, String token) {
    ArrayList<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("token", token));
    params.add(new BasicNameValuePair("content", "project"));
    params.add(new BasicNameValuePair("format", "json"));

    Response resp = doPost(url, params);
    int status = resp.getStatus();
    String projectInfo = resp.getContent();
    handleRedcapStatus(status, projectInfo);
    return projectInfo;
  }
  
  /**
   * Returns the metadata for a REDCap project in JSON format.
   * 
   * @param url The URL of the REDCap installation.
   * @param token The API token.
   * @param fieldIds The fields to request.
   * @return A response with a status and content.
   */
  public String getMetadata(String url, String token, Set<String> fieldIds) {
    ArrayList<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("token", token));
    params.add(new BasicNameValuePair("content", "metadata"));
    params.add(new BasicNameValuePair("format", "json"));
    for (String fieldId : fieldIds) {
      params.add(new BasicNameValuePair("fields[]", fieldId));
    }

    final Response resp = doPost(url, params);
    int status = resp.getStatus();
    if (status >= 200 && status < 300) {
      return resp.getContent();
    } else {
      handleRedcapStatus(status, resp.getContent());
      return null;
    }
  }
  
  /**
   * Returns the contents of a report in REDCap.
   * 
   * @param url The URL of the REDCap installation.
   * @param token The API token.
   * @param reportId The id of the REDCap report.
   * @return A response with a status and content.
   */
  public String getReport(String url, String token, String reportId) {
    ArrayList<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("token", token));
    params.add(new BasicNameValuePair("content", "report"));
    params.add(new BasicNameValuePair("report_id", reportId));
    params.add(new BasicNameValuePair("format", "json"));

    final Response resp = doPost(url, params);
    int status = resp.getStatus();
    if (status >= 200 && status < 300) {
      return resp.getContent();
    } else {
      handleRedcapStatus(status, resp.getContent());
      return null;
    }
  }

  private Response doPost(String url, ArrayList<NameValuePair> params) {
    HttpPost post = new HttpPost(url);
    post.setHeader("Content-Type", "application/x-www-form-urlencoded");

    try {
      post.setEntity(new UrlEncodedFormEntity(params));
    } catch (UnsupportedEncodingException e) {
      throw new RedmatchException("There was a problem related to encoding.", e);
    }

    StringBuffer result = new StringBuffer();
    HttpClient client = HttpClientBuilder.create().build();

    HttpResponse resp = null;
    try {
      resp = client.execute(post);
    } catch (IOException e) {
      throw new HttpException("There was an I/O issue communicating with REDCap at " + url, e,
          HttpStatus.BAD_GATEWAY);
    }
    

    
    int respCode = resp.getStatusLine().getStatusCode();

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(resp.getEntity().getContent()))) {
      String line = null;
      while ((line = reader.readLine()) != null) {
        result.append(line);
        result.append('\n');
      }
    } catch (Exception e) {
      throw new RedmatchException("", e);
    }
    
    final String content = result.toString();
    
    checkContentType(resp, content);

    return new Response(respCode, content);
  }

  private void checkContentType(HttpResponse resp, String content) {
    final Header[] contentTypeHeaders = resp.getHeaders("Content-Type");
    if (contentTypeHeaders.length >= 1) {
      final String contentType = contentTypeHeaders[0].getValue();
      if (!contentType.startsWith("application/json")) {
        if (log.isErrorEnabled()) {
          log.error("Unexpected REDCap response");
        }
        throw new HttpException("Expected a response from REDCap with content type "
            + "application/json but was " + contentType + ". Content = '" + content 
            + "'.", HttpStatus.BAD_GATEWAY);
      }
    } else {
      log.warn("Content-Type header was not present in REDCap response.");
    }
  }
  
  private void handleRedcapStatus(int status, String msg) {
    if (status >= 200 && status < 300) {
      // All good. No need to do anything.
    } else if (status == -1) {
      throw new HttpException("The REDCap server is down.", HttpStatus.BAD_GATEWAY);
    } else if (status == 403) {
      throw new HttpException(msg, HttpStatus.FORBIDDEN);
    } else if (status == 500) {
      throw new HttpException(msg, HttpStatus.BAD_GATEWAY);
    } else if (status == 401) {
      throw new HttpException(msg, HttpStatus.UNAUTHORIZED);
    } else if (status == 400) {
      throw new HttpException(msg, HttpStatus.BAD_REQUEST);
    } else if (status == 404 || status == 406 || status == 501) {
      throw new HttpException(
          "There seems to be a problem with the server configuration related to the access "
              + "to REDCap. The REDCap server responded with status " + status + " and message '"
              + msg + "'",
          HttpStatus.INTERNAL_SERVER_ERROR);
    } else {
      throw new HttpException("Unknown REDCap error. The REDCap server responded with status "
          + status + " and message '" + msg + "'", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

}
