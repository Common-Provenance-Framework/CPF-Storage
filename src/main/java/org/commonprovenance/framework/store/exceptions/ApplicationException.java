package org.commonprovenance.framework.store.exceptions;

public class ApplicationException extends RuntimeException {
  protected String className;
  protected String methodName;
  protected String detail;

  private boolean lockDetail;

  public ApplicationException() {
    super();

    this.lockDetail = false;
  }

  public ApplicationException(String message) {
    super(message);

    this.lockDetail = false;
  }

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);

    this.lockDetail = false;
  }

  @Override
  public String getMessage() {
    return className != null && methodName != null
        ? detail != null
            ? "[" + className + "][" + methodName + "](" + detail + "): " + super.getMessage()
            : "[" + className + "][" + methodName + "]: " + super.getMessage()
        : super.getMessage();
  }

  public void setHeader(String className, String methodName, String detail) {
    if (this.lockDetail)
      return;

    this.className = className;
    this.methodName = methodName;
    this.detail = detail;
    this.lockDetail = true;
  }

  public void setHeader(String className, String methodName) {
    if (this.lockDetail)
      return;

    this.className = className;
    this.methodName = methodName;
    this.lockDetail = true;
  }
}
