package org.commonprovenance.framework.store.web.trustedParty.dto.response;

public class TokenDataTPResponseDTO {
  private final String originatorId;
  private final String authorityId;
  private final Long tokenTimestamp;
  private final Long documentCreationTimestamp;
  private final String documentDigest;
  private final TokenAdditionalDataDataTPResponseDTO additionalData;

  public TokenDataTPResponseDTO(
      String originatorId,
      String authorityId,
      Long tokenTimestamp,
      Long documentCreationTimestamp,
      String documentDigest,
      TokenAdditionalDataDataTPResponseDTO additionalData) {
    this.originatorId = originatorId;
    this.authorityId = authorityId;
    this.tokenTimestamp = tokenTimestamp;
    this.documentCreationTimestamp = documentCreationTimestamp;
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

  public Long getDocumentCreationTimestamp() {
    return documentCreationTimestamp;
  }

  public String getDocumentDigest() {
    return documentDigest;
  }

  public TokenAdditionalDataDataTPResponseDTO getAdditionalData() {
    return additionalData;
  }
}
