package org.commonprovenance.framework.store.web.trustedParty.dto.response;

public class TokenTPResponseDTO {
  private final TokenDataTPResponseDTO data;
  private final String signature;

  public TokenTPResponseDTO(
      TokenDataTPResponseDTO data,
      String signature) {
    this.data = data;
    this.signature = signature;
  }

  public TokenDataTPResponseDTO getData() {
    return data;
  }

  public String getSignature() {
    return signature;
  }
}
