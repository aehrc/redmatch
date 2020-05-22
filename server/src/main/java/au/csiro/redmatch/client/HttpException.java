package au.csiro.redmatch.client;

import org.springframework.http.HttpStatus;

import au.csiro.redmatch.exceptions.AbstractRuntimeException;

/**
 * Thrown when there is a problem communicating with another server using HTTP.
 * 
 * @author Alejandro Metke Jimenez
 *
 */
public class HttpException extends AbstractRuntimeException {

  private final HttpStatus status;

  private static final long serialVersionUID = 1L;

  public HttpStatus getStatus() {
    return status;
  }

  public HttpException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace, HttpStatus status) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.status = status;
  }

  public HttpException(String message, Throwable cause, HttpStatus status) {
    super(message, cause);
    this.status = status;
  }

  public HttpException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

}
