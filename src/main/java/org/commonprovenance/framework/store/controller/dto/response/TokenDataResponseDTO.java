package org.commonprovenance.framework.store.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TokenDataResponse", description = "Canonical token data issued by the trusted party")
public class TokenDataResponseDTO {

  @Schema(description = "Originating organization identifier", example = "6fb292aa-ee38-48ae-998f-079ad9d01e7c")
  private final String originatorId;

  @Schema(description = "Trusted party name or authority identifier", example = "Trusted_Party")
  private final String authorityId;

  @Schema(description = "Token creation timestamp as Unix epoch seconds", example = "1774953179")
  private final Long tokenTimestamp;

  @Schema(description = "Original message timestamp as Unix epoch seconds", example = "1774953179")
  private final Long messageTimestamp;

  @Schema(description = "SHA-256 digest of the stored document", example = "abea81741dec2f2cb0bbc5999e51abe36196d7cfff077abbcbde8c1a2333de3a")
  private final String documentDigest;

  @Schema(description = "Additional token metadata", implementation = TokenAdditionalDataResponseDTO.class)
  private final TokenAdditionalDataResponseDTO additionalData;

  public TokenDataResponseDTO(
      String originatorId,
      String authorityId,
      Long tokenTimestamp,
      Long messageTimestamp,
      String documentDigest,
      TokenAdditionalDataResponseDTO additionalData) {
    this.originatorId = originatorId;
    this.authorityId = authorityId;
    this.tokenTimestamp = tokenTimestamp;
    this.messageTimestamp = messageTimestamp;
    this.documentDigest = documentDigest;
    this.additionalData = additionalData;
  }

  public String getOriginatorId() {
    return originatorId;
  }

  public String getAuthorityId() {
    return authorityId;
  }

  public Long getTokenTimestamp() {
    return tokenTimestamp;
  }

  public Long getMessageTimestamp() {
    return messageTimestamp;
  }

  public String getDocumentDigest() {
    return documentDigest;
  }

  public TokenAdditionalDataResponseDTO getAdditionalData() {
    return additionalData;
  }

}
