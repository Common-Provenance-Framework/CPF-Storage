package org.commonprovenance.framework.store.web.trustedParty.dto.response;

public class TokenTPResponseDTO {
  private final String jwt;

  public TokenTPResponseDTO(String jwt) {
    this.jwt = jwt;
  }

  public String getJwt() {
    return jwt;
  }
}
