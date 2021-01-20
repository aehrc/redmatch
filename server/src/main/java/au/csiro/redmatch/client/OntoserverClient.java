/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.client;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.UriType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

/**
 * Client to communicate with Ontoserver.
 * 
 * @author Alejandro Metke
 *
 */
@Component
@Qualifier("ontoserver")
public class OntoserverClient implements ITerminologyClient {
  
  /** Logger. */
  private static final Log log = LogFactory.getLog(OntoserverClient.class);
  
  @Value("${ontoserver.url}")
  private String ontoUrl;
  
  @Autowired
  private FhirContext ctx;
  
  @PostConstruct
  private void init() {
    log.info("Using Ontoserver at " + ontoUrl);
  }

  @Override
  public Parameters validateCode(String system, String code) {
    IGenericClient client = ctx.newRestfulGenericClient(ontoUrl);
    Parameters in = new Parameters();
    in.addParameter().setName("code").setValue(new CodeType(code));
    in.addParameter().setName("url").setValue(new UriType(system));
    
    Parameters out = client
      .operation()
      .onType(CodeSystem.class)
      .named("$validate-code")
      .withParameters(in)
      .execute();
    
    return out;
  }

  @Override
  public Parameters lookup(String system, String code, Collection<String> attributes) {
    IGenericClient client = ctx.newRestfulGenericClient(ontoUrl);
    Parameters in = new Parameters();
    in.addParameter().setName("code").setValue(new CodeType(code));
    in.addParameter().setName("system").setValue(new UriType(system));
    for (String attribute : attributes) {
      in.addParameter().setName("property").setValue(new CodeType(attribute));
    }
    
    Parameters out = client
      .operation()
      .onType(CodeSystem.class)
      .named("$lookup")
      .withParameters(in)
      .execute();
    
    return out;
  }

}
