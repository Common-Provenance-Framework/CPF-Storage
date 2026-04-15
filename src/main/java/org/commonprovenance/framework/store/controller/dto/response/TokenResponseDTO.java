package org.commonprovenance.framework.store.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TokenResponse", description = "JWT token response for a stored provenance document")
public class TokenResponseDTO {

  @Schema(description = "JWT token containing all token data and signature", example = "eyJhbGciOiJFUzI1NiIs...")
  private final String jwt;

  public TokenResponseDTO(String jwt) {
    this.jwt = jwt;
  }

  public String getJwt() {
    return jwt;
  }

}
