package au.csiro.redmatch.util;

import ca.uhn.fhir.context.FhirContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponent;


@JsonComponent
public class CustomBundleSerialiser extends JsonSerializer<Bundle> {
  
  @Autowired
  private FhirContext ctx;
  
  @Override
  public void serialize(Bundle bundle, JsonGenerator jsonGenerator, 
      SerializerProvider serializerProvider) throws IOException {
    final String jsonString = ctx.newJsonParser().encodeResourceToString(bundle);
    jsonGenerator.writeRaw(jsonString);
  }

}
