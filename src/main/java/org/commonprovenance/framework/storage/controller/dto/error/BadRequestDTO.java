package org.commonprovenance.framework.storage.controller.dto.error;

import java.util.List;

public class BadRequestDTO extends ErrorDTO {
  public BadRequestDTO(List<String> details) {
    super("ValidationError", details.stream().reduce("", (acc, i) -> {
      return acc.isBlank()
      ? i
      : acc + "; " + i;
    }));
  }
}