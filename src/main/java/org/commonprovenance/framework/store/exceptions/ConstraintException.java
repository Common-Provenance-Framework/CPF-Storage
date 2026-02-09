package org.commonprovenance.framework.store.exceptions;

public class ConstraintException extends ApplicationException {
  public ConstraintException() {
    super();
  }

  public ConstraintException(String message) {
    super(message);
  }

  public ConstraintException(Throwable cause) {
    super("Validator Exception: " + cause.getMessage(), cause);
  }

  public ConstraintException(String message, Throwable cause) {
    super(message, cause);
  }
}
