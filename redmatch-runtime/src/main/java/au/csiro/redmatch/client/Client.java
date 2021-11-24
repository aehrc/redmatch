/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.client;

import au.csiro.redmatch.model.Row;
import au.csiro.redmatch.model.Schema;

import java.util.List;
import java.util.Set;

/**
 * A client used to communicate with an external data source.
 *
 * @author Alejandro Metke Jimenez
 */
public interface Client {

  /**
   * Gets the schema of the external data source.
   *
   * @param endpoint The endpoint of the external data source.
   * @param credentials The credentials needed to authenticate to the external data source.
   * @return The schema of the external data source.
   */
  Schema getSchema(String endpoint, Credentials credentials);

  /**
   * Gets the schema of the external data source.
   *
   * @param endpoint The endpoint of the external data source.
   * @param credentials The credentials needed to authenticate to the external data source.
   * @param fieldIds The ids of the fields to fetch, if the source allows retrieving a subset of the schema.
   * @return The schema of the external data source.
   */
  Schema getSchema(String endpoint, Credentials credentials, Set<String> fieldIds);

  /**
   * Returns the data of the external data source.
   *
   * @param endpoint The endpoint of the external data source.
   * @param credentials The credentials needed to authenticate to the external data source.
   * @return A list of rows that represent the data of the external data source.
   */
  List<Row> getData(String endpoint, Credentials credentials);

  /**
   * Returns the data of the external data source.
   *
   * @param endpoint The endpoint of the external data source.
   * @param credentials The credentials needed to authenticate to the external data source.
   * @param fieldIds The ids of the fields to fetch, if the source allows retrieving a subset of the schema.
   * @return A list of rows that represent the data of the external data source.
   */
  List<Row> getData(String endpoint, Credentials credentials, Set<String> fieldIds);
}
