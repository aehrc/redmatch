/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */
package au.csiro.redmatch.util;

import java.io.IOException;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import ca.uhn.fhir.context.FhirContext;

/**
 * @author Alejandro Metke
 *
 */
public class CustomBundleDeserialiser extends JsonDeserializer<Bundle> {

  @Autowired
  private FhirContext ctx;
  
  @Override
  public Bundle deserialize(JsonParser jsonParser, DeserializationContext desCtx) 
      throws IOException {
    return ctx.newJsonParser().parseResource(Bundle.class, 
        jsonParser.readValueAsTree().toString());
  }

}
