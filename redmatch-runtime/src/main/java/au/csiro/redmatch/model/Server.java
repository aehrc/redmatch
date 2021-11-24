/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch.model;

/**
 * A definition of a data source.
 *
 * @author Alejandro Metke
 */
public class Server {
  public enum ServerType { REDCAP, CSV_OAUTH2 }
  private String name;
  private ServerType type;
  private String url;
  private String token;

  public Server (String name, String url, String token) {
    this.type = ServerType.REDCAP;
    this.name = name;
    this.url = url;
    this.token = token;
  }

  public String getName() {
    return name;
  }

  public ServerType getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }

  public String getToken() {
    return token;
  }
}
