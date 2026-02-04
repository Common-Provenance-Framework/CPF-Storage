package org.commonprovenance.framework.store.exceptions;

public class BadRequestException extends ApplicationException {
  public BadRequestException() {
    super();
  }

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(Throwable cause) {
    super("NotFound Exception: " + cause.getMessage(), cause);
  }

  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
