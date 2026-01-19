package org.commonprovenance.framework.storage.controller.dto.response;

public class DocumentResponseDTO {

  private final String identifier;
  private final String graph;
  private final String format;

  public DocumentResponseDTO(String identifier, String graph, String format) {
    this.identifier = identifier;
    this.graph = graph;
    this.format = format;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getGraph() {
    return graph;
  }

  public String getFormat() {
    return format;
  }
}
