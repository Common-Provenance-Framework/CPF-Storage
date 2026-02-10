package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dto.HasHashFunction;

public class TokenAdditionalDataDataTPResponseDTO implements HasHashFunction {
  private final String bundle;
  private final String hashFunction;
  private final String trustedPartyUri;
  private final String trustedPartyCertificate;

  public TokenAdditionalDataDataTPResponseDTO(
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
