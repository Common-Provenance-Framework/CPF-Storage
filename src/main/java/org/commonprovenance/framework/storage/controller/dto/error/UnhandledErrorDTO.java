package org.commonprovenance.framework.storage.controller.dto.error;

public class UnhandledErrorDTO extends ErrorDTO {
  public UnhandledErrorDTO() {
    super("InternalError", "*** Unhandled Server Error ***");
  }
}