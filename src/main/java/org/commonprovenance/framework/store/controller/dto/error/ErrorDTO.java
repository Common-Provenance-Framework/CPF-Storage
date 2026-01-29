package org.commonprovenance.framework.store.controller.dto.error;

public class ErrorDTO {
  private final String code;
  private final String message;

  public ErrorDTO(String code, String message) {
    this.code = code;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }
}
