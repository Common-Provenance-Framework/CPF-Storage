package org.commonprovenance.framework.store.controller.dto.error;

public class NotFoundDTO extends ErrorDTO {
  public NotFoundDTO(String details) {
    super("NotFound", details);
  }
}