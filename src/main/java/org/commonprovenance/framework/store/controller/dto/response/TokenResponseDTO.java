package org.commonprovenance.framework.store.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TokenResponse", description = "Trusted-party token response for a stored provenance document")
public class TokenResponseDTO {

  @Schema(description = "Structured token data used to compute and verify the token signature", implementation = TokenDataResponseDTO.class)
  private final TokenDataResponseDTO data;

  @Schema(description = "Base64-encoded signature of the token data", example = "MEUCIFiJHpqUvlt27O0TBxEDFfEaEvhyxnOp5QXNphgYnL9oAiEArkOZzBKcWfq1o3/DRZnX9kD1yG2dYzxl2SsyFxeFHDY=")
  private final String signature;

  public TokenResponseDTO(TokenDataResponseDTO data, String signature) {
    this.data = data;
    this.signature = signature;
  }

  public TokenDataResponseDTO getData() {
    return data;
  }

  public String getSignature() {
    return signature;
  }

}
