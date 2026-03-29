package org.commonprovenance.framework.store.controller.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "Standard error response")
public class ErrorDTO {
  @Schema(description = "Machine-readable error code", example = "ValidationError")
  private final String code;
  @Schema(description = "Human-readable error message", example = "documentFormat: Invalid format.")
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
