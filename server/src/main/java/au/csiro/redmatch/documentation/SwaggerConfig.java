/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.documentation;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger configuration.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

  /**
   * Bean used to activate Swagger.
   * 
   * @return The docket.
   */
  @Bean
  public Docket redmatchApi() {
    return new Docket(DocumentationType.SWAGGER_2).select()
        .apis(RequestHandlerSelectors.basePackage("au.csiro.redmatch.web")).build()
        .apiInfo(metaData());
  }

  private ApiInfo metaData() {
    ApiInfo apiInfo = new ApiInfo("Redmatch REST API",
        "REST API for Redmatch, a framework to expose REDCap data using FHIR.", "0.9.0", null,
        new Contact("Ontosever Support", "http://ontoserver.csiro.au",
            "ontoserver-support@csiro.au"),
        null, null, Collections.emptyList());
    return apiInfo;
  }
}
