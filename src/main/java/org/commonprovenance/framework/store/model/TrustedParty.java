package org.commonprovenance.framework.store.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrustedParty {
  private final Optional<UUID> id;
  private final String certificate;
  private final String url;
  private final Boolean isChecked;
  private final Boolean isValid;

  private final List<Organization> hasTrust;
  private final List<Token> hasIssued;

  public TrustedParty(
      UUID id,
      String certificate,
      String url,
      Boolean isChecked,
      Boolean isValid,
      List<Organization> hasTrust,
      List<Token> hasIssued) {
    this.id = Optional.ofNullable(id);
    this.certificate = certificate;
    this.url = url;
    this.isChecked = isChecked;
    this.isValid = isValid;
    this.hasTrust = hasTrust;
    this.hasIssued = hasIssued;
  }

  public TrustedParty(UUID id, String certificate, String url) {
    this.id = Optional.ofNullable(id);
    this.certificate = certificate;
    this.url = url;
    this.isChecked = false;
    this.isValid = false;
    this.hasTrust = Collections.emptyList();
    this.hasIssued = Collections.emptyList();
  }

  public TrustedParty withIsChecked(Boolean isChecked) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getCertificate(),
        this.getUrl(),
        isChecked,
        this.getIsValid(),
        this.getHasTrust(),
        this.getHasIssued());
  }

  public TrustedParty withIsValid(Boolean isValid) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getCertificate(),
        this.getUrl(),
        this.getIsChecked(),
        isValid,
        this.getHasTrust(),
        this.getHasIssued());
  }

  public TrustedParty withHasTrust(Organization organization) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getCertificate(),
        this.getUrl(),
        this.getIsChecked(),
        this.getIsValid(),
        Stream.concat(
            this.getHasTrust().stream(),
            Stream.of(organization))
            .collect(Collectors.toList()),
        this.getHasIssued());
  }

  public TrustedParty withHasIssued(Token token) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getCertificate(),
        this.getUrl(),
        this.getIsChecked(),
        this.getIsValid(),
        this.getHasTrust(),
        Stream.concat(
            this.getHasIssued().stream(),
            Stream.of(token))
            .collect(Collectors.toList()));
  }

  public Optional<UUID> getId() {
    return id;
  }

  public String getCertificate() {
    return certificate;
  }

  public String getUrl() {
    return url;
  }

  public Boolean getIsChecked() {
    return isChecked;
  }

  public Boolean getIsValid() {
    return isValid;
  }

  public List<Organization> getHasTrust() {
    return hasTrust;
  }

  public List<Token> getHasIssued() {
    return hasIssued;
  }
}
