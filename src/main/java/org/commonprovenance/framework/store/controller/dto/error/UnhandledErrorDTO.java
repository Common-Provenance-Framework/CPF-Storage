package org.commonprovenance.framework.store.controller.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UnhandledError", description = "Unexpected server failure")
public class UnhandledErrorDTO extends ErrorDTO {
  public UnhandledErrorDTO() {
    super("InternalError", "*** Unhandled Server Error ***");
  }
}