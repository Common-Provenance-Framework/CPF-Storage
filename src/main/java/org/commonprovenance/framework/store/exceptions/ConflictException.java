package org.commonprovenance.framework.store.exceptions;

public class ConflictException extends ApplicationException {
  public ConflictException() {
    super();
  }

  public ConflictException(String message) {
    super(message);
  }

  public ConflictException(Throwable cause) {
    super("Conflict Exception: " + cause.getMessage(), cause);
  }

  public ConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
