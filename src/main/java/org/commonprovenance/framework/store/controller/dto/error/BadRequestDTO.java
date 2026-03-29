package org.commonprovenance.framework.store.controller.dto.error;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BadRequestError", description = "Validation or business-rule error")
public class BadRequestDTO extends ErrorDTO {
  public BadRequestDTO(List<String> details) {
    super("ValidationError", details.stream().reduce("", (acc, i) -> {
      return acc.isBlank()
          ? i
          : acc + "; " + i;
    }));
  }
}