package au.csiro.redmatch.util;

import ca.uhn.fhir.context.FhirContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import org.hl7.fhir.r4.model.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonComponent;

@JsonComponent
public class CustomParametersSerialiser extends JsonSerializer<Parameters> {
  
  @Autowired
  private FhirContext ctx;
  
  @Override
  public void serialize(Parameters params, JsonGenerator jsonGenerator, 
      SerializerProvider serializerProvider) throws IOException {
    final String jsonString = ctx.newJsonParser().encodeResourceToString(params);
    jsonGenerator.writeRaw(jsonString);
  }

}
