package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dto.HasJwtToken;

public class TokenTPResponseDTO implements
    HasJwtToken<TokenTPResponseDTO> {
  private final String jwt;

  public TokenTPResponseDTO(String jwt) {
    this.jwt = jwt;
  }

  public String getJwt() {
    return jwt;
  }

}
