/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.util;

import ca.uhn.fhir.context.FhirContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

import org.hl7.fhir.r4.model.OperationOutcome;

public class CustomOperationOutcomeSerialiser extends JsonSerializer<OperationOutcome> {

  private FhirContext ctx;

  public CustomOperationOutcomeSerialiser(FhirContext ctx) {
    this.ctx = ctx;
  }
  
  @Override
  public void serialize(OperationOutcome oo, JsonGenerator jsonGenerator, 
      SerializerProvider serializerProvider) throws IOException {
    final String jsonString = ctx.newJsonParser().encodeResourceToString(oo);
    jsonGenerator.writeRaw(jsonString);
  }

}
