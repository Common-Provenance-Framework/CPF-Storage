package org.commonprovenance.framework.store.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TokenAdditionalDataResponse", description = "Additional metadata included in trusted-party token data")
public class TokenAdditionalDataResponseDTO {

  @Schema(description = "Bundle URI referenced by the token", example = "http://localhost:8080/api/v1/documents/DnaSequencingBundle_V0")
  private final String bundle;

  @Schema(description = "Hash function used to compute the document digest", example = "SHA256")
  private final String hashFunction;

  @Schema(description = "Trusted party API base URI", example = "trusted-party:8020")
  private final String trustedPartyUri;

  @Schema(description = "PEM-encoded trusted party certificate", example = "-----BEGIN CERTIFICATE-----...-----END CERTIFICATE-----")
  private final String trustedPartyCertificate;

  public TokenAdditionalDataResponseDTO(
      String bundle,
      String hashFunction,
      String trustedPartyUri,
      String trustedPartyCertificate) {
    this.bundle = bundle;
    this.hashFunction = hashFunction;
    this.trustedPartyUri = trustedPartyUri;
    this.trustedPartyCertificate = trustedPartyCertificate;
  }

  public String getBundle() {
    return bundle;
  }

  public String getHashFunction() {
    return hashFunction;
  }

  public String getTrustedPartyUri() {
    return trustedPartyUri;
  }

  public String getTrustedPartyCertificate() {
    return trustedPartyCertificate;
  }

}
