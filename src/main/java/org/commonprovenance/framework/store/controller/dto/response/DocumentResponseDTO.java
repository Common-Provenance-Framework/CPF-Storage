package org.commonprovenance.framework.store.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DocumentResponse", description = "Stored provenance document response")
public class DocumentResponseDTO {

  @Schema(description = "Stored provenance document payload", example = "ewogICJidW5kbGUiOiB7IC4uLiB9Cn0=")
  private final String graph;

  @Schema(description = "Token issued for the returned document", implementation = TokenResponseDTO.class)
  private final TokenResponseDTO token;

  public DocumentResponseDTO(String graph, TokenResponseDTO token) {
    this.graph = graph;
    this.token = token;
  }

  public DocumentResponseDTO() {
    this.graph = null;
    this.token = null;
  }

  public DocumentResponseDTO withGraph(String graph) {
    return new DocumentResponseDTO(
        graph,
        this.getToken());
  }

  public DocumentResponseDTO withToken(TokenResponseDTO token) {
    return new DocumentResponseDTO(
        this.getGraph(),
        token);
  }

  public String getGraph() {
    return graph;
  }

  public TokenResponseDTO getToken() {
    return token;
  }

}
