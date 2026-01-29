package org.commonprovenance.framework.store.controller.dto.error;

public class InternalServerErrorDTO extends ErrorDTO {
  public InternalServerErrorDTO() {
    super("InternalError", "*** Internal Server Error ***");
  }
}