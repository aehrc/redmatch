package au.csiro.redmatch.importer;

/**
 * Thrown when there is a problem importing a schema.
 *
 * @author Alejandro Metke-Jimenez
 */
public class ImportingException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public ImportingException() {

  }

  public ImportingException(String message) {
    super(message);
  }

  public ImportingException(Throwable cause) {
    super(cause);
  }

  public ImportingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImportingException(String message, Throwable cause, boolean enableSuppression,
                              boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
