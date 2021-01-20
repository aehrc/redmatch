/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.client;

/**
 * Represents a response of an interaction with REDCap.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class Response {

  private int status;

  private String content;
  
  private byte[] compressedContent;

  public Response() {

  }
  
  /**
   * Creates a new response with a status and a content string.
   * 
   * @param status The status.
   * @param content The content string.
   */
  public Response(int status, String content) {
    this.status = status;
    this.content = content;
  }
  
  public Response(int status, byte[] compressedContent) {
    this.status = status;
    this.compressedContent = compressedContent;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public byte[] getCompressedContent() {
    return compressedContent;
  }

  public void setCompressedContent(byte[] compressedContent) {
    this.compressedContent = compressedContent;
  }

}
