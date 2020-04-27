/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use is subject to 
 * license terms and conditions.
 */

package au.csiro.redmatch.util;

import ca.uhn.fhir.context.FhirContext;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import au.csiro.redmatch.exporter.HapiReflectionHelper;

/**
 * {@link HttpMessageConverter} for FHIR bundles. Required in integration tests.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class BundleHttpMessageConverter extends AbstractHttpMessageConverter<Bundle> {
  
  private FhirContext ctx;
  
  public BundleHttpMessageConverter(FhirContext ctx) {
    super(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
    this.ctx = ctx;
  }
  
  @Override
  protected boolean supports(Class<?> clazz) {
    if (clazz.getName().equals(HapiReflectionHelper.FHIR_TYPES_BASE_PACKAGE + ".Bundle")) {
      return true;
    } else {
      return false;
    }
  }
  
  @Override
  protected Bundle readInternal(Class<? extends Bundle> clazz, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    final Reader reader = new InputStreamReader(inputMessage.getBody(), Charset.forName("UTF8"));
    return ctx.newJsonParser().parseResource(Bundle.class, reader);
  }

  @Override
  protected void writeInternal(Bundle t, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    final Writer writer = new OutputStreamWriter(outputMessage.getBody(), Charset.forName("UTF8"));
    ctx.newJsonParser().encodeResourceToWriter(t, writer);
  }

}
