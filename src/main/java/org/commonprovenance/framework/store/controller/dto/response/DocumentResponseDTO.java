package org.commonprovenance.framework.store.controller.dto.response;

public class DocumentResponseDTO {

  private final String identifier;
  private final String organizationIdentifier;
  private final String graph;
  private final String format;

  public DocumentResponseDTO(String identifier, String organizationIdentifier, String graph, String format) {
    this.identifier = identifier;
    this.organizationIdentifier = organizationIdentifier;
    this.graph = graph;
    this.format = format;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getOrganizationIdentifier() {
    return organizationIdentifier;
  }

  public String getGraph() {
    return graph;
  }

  public String getFormat() {
    return format;
  }
}
