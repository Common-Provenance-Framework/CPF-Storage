package org.commonprovenance.framework.store.controller.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NotFoundError", description = "Resource was not found")
public class NotFoundDTO extends ErrorDTO {
  public NotFoundDTO(String details) {
    super("NotFound", details);
  }
}