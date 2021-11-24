package au.csiro.redmatch.client;

/**
 * Thrown when there is an issue in the interaction with a data source.
 *
 * @author Alejandro Metke Jimenez
 */
public class ClientException extends RuntimeException {

  /**
   * Constructor.
   *
   * @param message An error message.
   */
  public ClientException(String message) {
    super(message);
  }

  /**
   * Constructor.
   *
   * @param message An error message.
   * @param cause The cause of the error.
   */
  public ClientException(String message, Throwable cause) {
    super(message, cause);
  }
}
