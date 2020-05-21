/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com). All rights reserved. 
 * Use is subject to license terms and conditions.
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
