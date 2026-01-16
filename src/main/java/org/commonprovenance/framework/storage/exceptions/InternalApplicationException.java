package org.commonprovenance.framework.storage.exceptions;

public class InternalApplicationException extends ApplicationException {
  public InternalApplicationException() {
    super();
  }

  public InternalApplicationException(String message) {
    super(message);
  }

  public InternalApplicationException(Throwable cause) {
    super("Unhandled Exception: " + cause.getMessage(), cause);
  }

  public InternalApplicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
