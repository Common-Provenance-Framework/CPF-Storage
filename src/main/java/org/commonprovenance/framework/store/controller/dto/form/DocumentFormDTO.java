package org.commonprovenance.framework.store.controller.dto.form;

import org.commonprovenance.framework.store.controller.validator.IsBase64String;
import org.commonprovenance.framework.store.controller.validator.IsJsonBase64;
import org.commonprovenance.framework.store.controller.validator.IsProvBase64Json;
import org.commonprovenance.framework.store.controller.validator.IsValueOfEnum;
import org.commonprovenance.framework.store.model.Format;

import jakarta.validation.constraints.NotBlank;

public class DocumentFormDTO {

  @NotBlank(message = "Graph should not be null or empty.")
  @IsBase64String(message = "Graph should be Base64 string.")
  @IsJsonBase64(message = "Graph should be Base64 json string")
  @IsProvBase64Json(message = "Graph should be a Base64 provenance json string")
  private final String graph;

  @NotBlank(message = "Format should not be null or empty.")
  @IsValueOfEnum(enumClass = Format.class, message = "Invalid format.")
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
