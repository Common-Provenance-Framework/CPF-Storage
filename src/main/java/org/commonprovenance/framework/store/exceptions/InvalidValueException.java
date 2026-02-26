package org.commonprovenance.framework.store.exceptions;

public class InvalidValueException extends ApplicationException {
  public InvalidValueException() {
    super();
  }

  public InvalidValueException(String message) {
    super(message);
  }

  public InvalidValueException(Throwable cause) {
    super("NotFound Exception: " + cause.getMessage(), cause);
  }

  public InvalidValueException(String message, Throwable cause) {
    super(message, cause);
  }
}
