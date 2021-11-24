package au.csiro.redmatch.client;

/**
 * Credentials for a REDCap API.
 *
 * @author Alejandro Metke Jimenez
 */
public class RedcapCredentials extends Credentials {
  private String token;

  public RedcapCredentials(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }
}
