package org.commonprovenance.framework.store.model;

public final class AdditionalData {
  private final String bundle;
  private final String organizationIdentifier;
  private final String hashFunction;
  private final String trustedPartyUri;
  private final String trustedPartyCertificate;
  private final Long documentTimestamp;

  public AdditionalData(
      String bundle,
      String organizationIdentifier,
      String hashFunction,
      String trustedPartyUri,
      String trustedPartyCertificate,
      Long documentTimestamp

  ) {
    this.bundle = bundle;
    this.organizationIdentifier = organizationIdentifier;
    this.hashFunction = hashFunction;
    this.trustedPartyUri = trustedPartyUri;
    this.trustedPartyCertificate = trustedPartyCertificate;
    this.documentTimestamp = documentTimestamp;
  }

  public String getBundle() {
    return bundle;
  }

  public String getOrganizationIdentifier() {
    return organizationIdentifier;
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