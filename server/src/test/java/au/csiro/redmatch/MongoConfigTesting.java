/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.MongoClients;

@Configuration
@Profile(value = {"test"})
public class MongoConfigTesting {
  
  @Bean
  public MongoTemplate mongoTemplate() {
    return new MongoTemplate(MongoClients.create("mongodb://localhost:27017"), "test");
  }
  
}
