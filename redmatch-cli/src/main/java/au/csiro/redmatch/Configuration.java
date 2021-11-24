package au.csiro.redmatch;

import java.util.List;

public class Configuration {
  private List<Server> servers;

  public List<Server> getServers() {
    return servers;
  }

  public void setServers(List<Server> servers) {
    this.servers = servers;
  }
}
