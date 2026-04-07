package org.commonprovenance.framework.store.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DocumentResponse", description = "Stored provenance document response")
public class DocumentResponseDTO {

  @Schema(description = "Stored provenance document payload", example = "ewogICJidW5kbGUiOiB7IC4uLiB9Cn0=")
  private final String document;

  @Schema(description = "Token issued for the returned document", implementation = TokenResponseDTO.class)
  private final TokenResponseDTO token;

  public DocumentResponseDTO(String document, TokenResponseDTO token) {
    this.document = document;
    this.token = token;
  }

  public String getDocument() {
    return document;
  }

  public TokenResponseDTO getToken() {
    return token;
  }

}
