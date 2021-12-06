/*
 * Copyright © 2018-2021, Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * Licensed under the CSIRO Open Source Software Licence Agreement.
 */
package au.csiro.redmatch;

import java.util.List;

/**
 * Represents a Redmatch configuration file.
 *
 * @author Alejandro Metke Jimenez
 */
public class Configuration {

  /**
   * List of servers. These can be referenced from the Redmatch rules.
   */
  private List<Server> servers;

  public List<Server> getServers() {
    return servers;
  }

  public void setServers(List<Server> servers) {
    this.servers = servers;
  }
}
