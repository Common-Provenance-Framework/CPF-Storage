package org.commonprovenance.framework.store.model;

import java.util.Date;
import java.util.UUID;

public class Certificate {
  private final UUID id;
  private final Organization organization;
  private final String digest;
  private final String cert;
  private final CertificateType type;
  private final Boolean revoked;
  private final Date received;

  public Certificate(
      UUID id,
      Organization organization,
      String digest,
      String cert,
      CertificateType type,
      Boolean revoked,
      Date received) {
    this.id = id;
    this.organization = organization;
    this.digest = digest;
    this.cert = cert;
    this.type = type;
    this.revoked = revoked;
    this.received = received;
  }

  public UUID getId() {
    return id;
  }

  public Organization getOrganization() {
    return organization;
  }

  public String getDigest() {
    return digest;
  }

  public String getCert() {
    return cert;
  }

  public CertificateType getType() {
    return type;
  }

  public Boolean getRevoked() {
    return revoked;
  }

  public Date getReceived() {
    return received;
  }

}
