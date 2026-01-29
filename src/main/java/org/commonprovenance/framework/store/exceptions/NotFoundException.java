package org.commonprovenance.framework.store.exceptions;

public class NotFoundException extends ApplicationException {
  public NotFoundException() {
    super();
  }

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(Throwable cause) {
    super("NotFound Exception: " + cause.getMessage(), cause);
  }

  public NotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
