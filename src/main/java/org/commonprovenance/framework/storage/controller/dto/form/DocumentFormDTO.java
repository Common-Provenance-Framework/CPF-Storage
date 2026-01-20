package org.commonprovenance.framework.storage.controller.dto.form;

import org.commonprovenance.framework.storage.controller.validator.ValueOfBase64String;
import org.commonprovenance.framework.storage.controller.validator.ValueOfEnum;
import org.commonprovenance.framework.storage.model.Format;
import jakarta.validation.constraints.NotBlank;

public class DocumentFormDTO {

  @NotBlank(message = "Graph should not be null or empty.")
  @ValueOfBase64String( message = "Graph should be base64 string.")
  private final String graph;

  @NotBlank(message = "Format should not be null or empty.")
  @ValueOfEnum(enumClass = Format.class, message = "Invalid format.")
  private final String format;

  public DocumentFormDTO(String graph, String format) {
    this.graph = graph;
    this.format = format;
  }

  public String getGraph() {
    return graph;
  }

  public String getFormat() {
    return format;
  }
}
