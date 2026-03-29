package org.commonprovenance.framework.store.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DocumentResponse", description = "Stored provenance document response")
public class DocumentResponseDTO {

  @Schema(description = "Document identifier", example = "SamplingBundle_V1")
  private final String identifier;
  @Schema(description = "Owner organization identifier", example = "ORG1")
  private final String organizationIdentifier;
  @Schema(description = "Document graph payload (usually Base64 encoded)", example = "eyJwcm92On...")
  private final String graph;
  @Schema(description = "Document format", example = "JSON")
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
