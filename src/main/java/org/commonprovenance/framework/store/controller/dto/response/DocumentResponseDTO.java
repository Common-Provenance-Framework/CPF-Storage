package org.commonprovenance.framework.store.controller.dto.response;

import org.commonprovenance.framework.store.common.dto.HasGraph;
import org.commonprovenance.framework.store.common.dto.HasTokenResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DocumentResponse", description = "Stored provenance document response")
public class DocumentResponseDTO implements
    HasGraph<DocumentResponseDTO>,
    HasTokenResponse<DocumentResponseDTO> {

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

  @Override
  public DocumentResponseDTO withGraph(String graph) {
    return new DocumentResponseDTO(
        graph,
        this.getToken());
  }

  @Override
  public DocumentResponseDTO withToken(TokenResponseDTO token) {
    return new DocumentResponseDTO(
        this.getGraph(),
        token);
  }

  @Override
  public String getGraph() {
    return graph;
  }

  public TokenResponseDTO getToken() {
    return token;
  }

}
