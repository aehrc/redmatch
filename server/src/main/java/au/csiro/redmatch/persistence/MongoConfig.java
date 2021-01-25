/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research
 * Organisation (CSIRO) ABN 41 687 119 230. Licensed under the CSIRO Open Source
 * Software Licence Agreement.
 */
package au.csiro.redmatch.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * MongoDB configuration used for development. Launches an embedded MongoDB instance.
 * 
 * @author Alejandro Metke
 *
 */
@Configuration
@Profile(value = {"dev"})
public class MongoConfig {
  
  public @Bean MongoClient mongoClient() {
    return MongoClients.create("mongodb://localhost:27017");
  }
  
  public @Bean MongoTemplate mongoTemplate() {
    MongoTemplate res = new MongoTemplate(mongoClient(), "redmatch");
    res.setWriteResultChecking(WriteResultChecking.EXCEPTION);
    return res;
  }

}
