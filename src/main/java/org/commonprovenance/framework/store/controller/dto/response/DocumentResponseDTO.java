package org.commonprovenance.framework.store.controller.dto.response;

public class DocumentResponseDTO {

  private final String id;
  private final String graph;
  private final String format;

  public DocumentResponseDTO(String id, String graph, String format) {
    this.id = id;
    this.graph = graph;
    this.format = format;
  }

  public String getId() {
    return id;
  }

  public String getGraph() {
    return graph;
  }

  public String getFormat() {
    return format;
  }
}
