package org.commonprovenance.framework.storage.exceptions;

public class ApplicationException extends RuntimeException {
  public ApplicationException() {
    super();
  }

  public ApplicationException(String message) {
    super(message);
  }

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
