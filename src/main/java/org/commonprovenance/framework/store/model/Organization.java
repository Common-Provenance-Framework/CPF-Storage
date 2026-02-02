package org.commonprovenance.framework.store.model;

import java.util.List;
import java.util.UUID;

public class Organization {
  private final UUID id;
  private final String name;
  private final String clientCertificate;
  private final List<String> intermediateCertificates;
  private final TrustedParty trustedParty;

  public Organization(
      UUID id,
      String name,
      String clientCertificate,
      List<String> intermediateCertificates,
      TrustedParty trustedParty) {
    this.id = id;
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
    this.trustedParty = trustedParty;
  }

  public Organization withId(UUID id) {
    return new Organization(
        id,
        this.getName(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        this.getTrustedParty());
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getClientCertificate() {
    return clientCertificate;
  }

  public List<String> getIntermediateCertificates() {
    return intermediateCertificates;
  }

  public TrustedParty getTrustedParty() {
    return trustedParty;
  }

}
