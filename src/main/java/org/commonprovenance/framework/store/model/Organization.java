package org.commonprovenance.framework.store.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.store.common.dto.HasIdentifier;

public class Organization implements HasIdentifier<Organization> {
  private final Optional<String> id;
  private final String identifier;
  private final String clientCertificate;
  private final List<String> intermediateCertificates;
  private final Optional<TrustedParty> trustedParty;

  public Organization() {
    this.id = Optional.empty();
    this.identifier = null;
    this.clientCertificate = null;
    this.intermediateCertificates = Collections.emptyList();
    this.trustedParty = Optional.empty();
  }

  public Organization(
      String id,
      String identifier,
      String clientCertificate,
      List<String> intermediateCertificates,
      TrustedParty trustedParty) {
    this.id = Optional.ofNullable(id);
    this.identifier = identifier;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
    this.trustedParty = Optional.ofNullable(trustedParty);
  }

  public Organization(
      String identifier,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.id = Optional.empty();
    this.identifier = identifier;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
    this.trustedParty = Optional.empty();
  }

  public Organization withId(String id) {
    return new Organization(
        id,
        this.getIdentifier(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        this.getTrustedParty().orElse(null));
  }

  public Organization withIdentifier(String identifier) {
    return new Organization(
        this.getId().orElse(null),
        identifier,
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        this.getTrustedParty().orElse(null));
  }

  public Organization withTrustedParty(TrustedParty trustedParty) {
    return new Organization(
        this.getId().orElse(null),
        this.getIdentifier(),
        this.getClientCertificate(),
        this.getIntermediateCertificates(),
        trustedParty);
  }

  public Optional<String> getId() {
    return id;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getClientCertificate() {
    return clientCertificate;
  }

  public List<String> getIntermediateCertificates() {
    return intermediateCertificates;
  }

  public Optional<TrustedParty> getTrustedParty() {
    return trustedParty;
  }

}
