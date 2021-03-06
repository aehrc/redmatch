/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;

@SpringBootApplication
public class Application {

  /**
   * Created here as a bean because it is expensive to create and we only need one instance that 
   * can be shared.
   *
   * @return FHIRContext The FHIR context for the entire application.
   */
  @Bean
  public FhirContext fhirContext() {
    return FhirContext.forR4();
  }
  
  @Bean
  public Gson gson() {
    return new Gson();
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
