/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptDefinitionDesignationComponent;
import org.hl7.fhir.r4.model.CodeSystem.ConceptPropertyComponent;
import org.hl7.fhir.r4.model.CodeType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;

import au.csiro.redmatch.client.ITerminologyClient;
import au.csiro.redmatch.client.OntoserverClient;

import org.hl7.fhir.r4.model.Type;

import ca.uhn.fhir.context.FhirContext;

/**
 * Mock implementation of {@link OntoserverClient} for testing.
 * 
 * @author Alejandro Metke
 *
 */
public class MockTerminolgyClient implements ITerminologyClient {
  
  private static final Log log = LogFactory.getLog(MockTerminolgyClient.class);
  
  private Map<String, Map<String, ConceptDefinitionComponent>> codeMap = new HashMap<>();
  
  public MockTerminolgyClient() {
    log.info("Loading Redmatch grammar code system into memory");
    
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("preload.json").getFile());
    String absolutePath = file.getAbsolutePath();
    log.debug("Loading grammar code system from " + absolutePath);
    
    FhirContext ctx = FhirContext.forR4();
    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(absolutePath))) {
      Bundle b = (Bundle) ctx.newJsonParser().parseResource(reader);
      for (BundleEntryComponent bec : b.getEntry()) {
        CodeSystem cs = (CodeSystem) bec.getResource();
        final Map<String, ConceptDefinitionComponent> map = new HashMap<>();
        codeMap.put(cs.getUrl(), map);
        for (ConceptDefinitionComponent cdc : cs.getConcept()) {
          map.put(cdc.getCode(), cdc);
          for (ConceptDefinitionDesignationComponent designation : cdc.getDesignation()) {
            map.put(designation.getValue(), cdc);
          }
        }
      }
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public Parameters validateCode(String system, String code) {
    final Map<String, ConceptDefinitionComponent> map = codeMap.get(system);
    if (map == null) {
      throw new IllegalArgumentException("System " + system + " is not supported by mock "
          + "implementation.");
    }
    
    boolean valid = map.containsKey(code);
    Parameters out = new Parameters();
    out.addParameter().setName("result").setValue(new BooleanType(valid));
    return out;
  }

  @Override
  public Parameters lookup(String system, String code, Collection<String> attributes) {
    final Map<String, ConceptDefinitionComponent> map = codeMap.get(system);
    if (map == null) {
      throw new IllegalArgumentException("System " + system + " is not supported by mock "
          + "implementation.");
    }
    
    final Set<String> set = new HashSet<>(attributes);
    Parameters out = new Parameters();
    ConceptDefinitionComponent cdc = map.get(code);
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
