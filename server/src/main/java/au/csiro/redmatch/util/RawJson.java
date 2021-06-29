/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Utility class to return raw JSON in the REST API. Idea taken from
 * https://righele.it/2018/12/07/how-do-you-return-a-raw-json-with-jackson.
 *
 * @author Alejandro Metke
 */
public class RawJson {
  private String payload;

  public RawJson(String payload) {
    this.payload = payload;
  }

  public static RawJson from(String payload) {
    return new RawJson(payload);
  }

  @JsonValue
  @JsonRawValue
  public String getPayload() {
    return this.payload;
  }
}
