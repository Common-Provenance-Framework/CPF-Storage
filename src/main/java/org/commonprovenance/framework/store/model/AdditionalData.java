package org.commonprovenance.framework.store.model;

public final class AdditionalData {
  private final String bundle;
  private final String originatorName;
  private final String hashFunction;
  private final String trustedPartyUri;
  private final String trustedPartyCertificate;
  private final Long documentTimestamp;

  public AdditionalData(
      String bundle,
      String originatorName,

      String hashFunction,
      String trustedPartyUri,
      String trustedPartyCertificate,
      Long documentTimestamp

  ) {
    this.bundle = bundle;
    this.originatorName = originatorName;
    this.hashFunction = hashFunction;
    this.trustedPartyUri = trustedPartyUri;
    this.trustedPartyCertificate = trustedPartyCertificate;
    this.documentTimestamp = documentTimestamp;
  }

  public String getBundle() {
    return bundle;
  }

  public String getOriginatorName() {
    return originatorName;
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

  public Long getDocumentTimestamp() {
    return documentTimestamp;
  }
}