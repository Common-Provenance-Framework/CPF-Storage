package org.commonprovenance.framework.store.controller.dto.response;

import org.commonprovenance.framework.store.common.dto.HasJwtToken;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TokenResponse", description = "JWT token response for a stored provenance document")
public class TokenResponseDTO implements
    HasJwtToken<TokenResponseDTO> {

  @Schema(description = "JWT token containing all token data and signature", example = "eyJhbGciOiJFUzI1NiIs...")
  private final String jwt;

  public TokenResponseDTO(String jwt) {
    this.jwt = jwt;
  }

  public TokenResponseDTO() {
    this.jwt = null;
  }

  @Override
  public TokenResponseDTO withJwt(String jwt) {
    return new TokenResponseDTO(jwt);
  }

  @Override
  public String getJwt() {
    return jwt;
  }

}
