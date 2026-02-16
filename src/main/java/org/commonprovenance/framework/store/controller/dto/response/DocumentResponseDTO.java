package org.commonprovenance.framework.store.controller.dto.response;

public class DocumentResponseDTO {

  private final String id;
  private final String organizationId;
  private final String graph;
  private final String format;

  public DocumentResponseDTO(String id, String organizationId, String graph, String format) {
    this.id = id;
    this.organizationId = organizationId;
    this.graph = graph;
    this.format = format;
  }

  public String getId() {
    return id;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public String getGraph() {
    return graph;
  }

  public String getFormat() {
    return format;
  }
}
