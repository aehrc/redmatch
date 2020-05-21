/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. Use 
 * is subject to license terms and conditions.
 */
package au.csiro.redmatch.validation;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionDesignationComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.CodeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

import au.csiro.redmatch.client.ITerminologyServer;
import au.csiro.redmatch.client.OntoserverClient;

import org.hl7.fhir.r4.model.Type;

import ca.uhn.fhir.context.FhirContext;

/**
 * Mock implementation of {@link OntoserverClient} for testing.
 * 
 * @author Alejandro Metke
 *
 */
public class MockTerminolgyServer implements ITerminologyServer {
  
  private static final Log log = LogFactory.getLog(MockTerminolgyServer.class);
  
  private Map<String, ConceptDefinitionComponent> codeMap = new HashMap<>();
  
  public MockTerminolgyServer() {
    log.info("Loading Redmatch grammar code system into memory");
    FhirContext ctx = FhirContext.forR4();
    try (InputStreamReader reader = new InputStreamReader(
        new FileInputStream("src/test/resources/preload.json"))) {
      Bundle b = (Bundle) ctx.newJsonParser().parseResource(reader);
      CodeSystem cs = (CodeSystem) b.getEntryFirstRep().getResource();
      for (ConceptDefinitionComponent cdc : cs.getConcept()) {
        codeMap.put(cdc.getCode(), cdc);
        for (ConceptDefinitionDesignationComponent designation : cdc.getDesignation()) {
          codeMap.put(designation.getValue(), cdc);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public Parameters validateCode(String system, String code) {
    if (!system.equals("http://csiro.au/redmatch-fhir")) {
      throw new IllegalArgumentException("Mock implementation only supports system "
          + "http://csiro.au/redmatch-fhir.");
    }
    
    boolean valid = codeMap.containsKey(code);
    Parameters out = new Parameters();
    out.addParameter().setName("result").setValue(new BooleanType(valid));
    return out;
  }

  @Override
  public Parameters lookup(String system, String code, Collection<String> attributes) {
    if (!system.equals("http://csiro.au/redmatch-fhir")) {
      throw new IllegalArgumentException("Mock implementation only supports system "
          + "http://csiro.au/redmatch-fhir.");
    }
    if (!codeMap.containsKey(code)) {
      throw new IllegalArgumentException("Code " + code + " is not valid.");
    }
    
    final Set<String> set = new HashSet<>(attributes);
    Parameters out = new Parameters();
    ConceptDefinitionComponent cdc = codeMap.get(code);
    for (ConceptPropertyComponent cpc : cdc.getProperty()) {
      String propCode = cpc.getCode();
      if (set.contains(propCode)) {
        ParametersParameterComponent ppc = out.addParameter().setName("property");
        ppc.addPart().setName("code").setValue(new CodeType(propCode));
        Type val = cpc.getValue();
        String className = val.getClass().getSimpleName();
        String typeName = className.substring(0, className.length() - 4);
        ppc.addPart().setName("value" + typeName).setValue(val);
        
      }
    }
    return out;
  }

}
