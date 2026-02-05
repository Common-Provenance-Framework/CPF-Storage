package org.commonprovenance.framework.store.model;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class Certificate {
  private final Optional<UUID> id;
  private final Optional<Organization> organization;
  private final String digest;
  private final String cert;
  private final Optional<CertificateType> type;
  private final Boolean revoked;
  private final Optional<ZonedDateTime> received;

  public Certificate(
      UUID id,
      Organization organization,
      String digest,
      String cert,
      CertificateType type,
      Boolean revoked,
      ZonedDateTime received) {
    this.id = Optional.ofNullable(id);
    this.organization = Optional.ofNullable(organization);
    this.digest = digest;
    this.cert = cert;
    this.type = Optional.ofNullable(type);
    this.revoked = revoked;
    this.received = Optional.ofNullable(received);
  }

  public Optional<UUID> getId() {
    return id;
  }

  public Optional<Organization> getOrganization() {
    return organization;
  }

  public String getDigest() {
    return digest;
  }

  public String getCert() {
    return cert;
  }

  public Optional<CertificateType> getType() {
    return type;
  }

  public Boolean getRevoked() {
    return revoked;
  }

  public Optional<ZonedDateTime> getReceived() {
    return received;
  }

}
