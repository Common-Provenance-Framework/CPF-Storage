package org.commonprovenance.framework.store.controller.dto.error;

public class UnhandledErrorDTO extends ErrorDTO {
  public UnhandledErrorDTO() {
    super("InternalError", "*** Unhandled Server Error ***");
  }
}