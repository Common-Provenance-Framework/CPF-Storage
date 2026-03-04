package org.commonprovenance.framework.store.exceptions;

public class ArgumentValidatorException extends ApplicationException {
  public ArgumentValidatorException() {
    super();
  }

  public ArgumentValidatorException(String message) {
    super(message);
  }

  public ArgumentValidatorException(Throwable cause) {
    super("Illetal Argument Exception: " + cause.getMessage(), cause);
  }

  public ArgumentValidatorException(String message, Throwable cause) {
    super(message, cause);
  }
}
