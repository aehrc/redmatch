/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch;

/**
 * Represents a server in the Redmatch configuration file. Servers can be referenced in the Redmatch rules by name. The
 * actual details required to connect to the server are available in the configuration file.
 *
 * @author Alejandro Metke Jimenez
 */
public class Server {
  private String name;
  private String type;
  private String url;
  private String token;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
