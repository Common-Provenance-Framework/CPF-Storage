package org.commonprovenance.framework.store.controller.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InternalServerError", description = "Internal application failure")
public class InternalServerErrorDTO extends ErrorDTO {
  public InternalServerErrorDTO() {
    super("InternalError", "*** Internal Server Error ***");
  }
}