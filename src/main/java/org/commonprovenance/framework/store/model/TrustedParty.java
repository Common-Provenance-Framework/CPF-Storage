package org.commonprovenance.framework.store.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TrustedParty {
  private final Optional<UUID> id;
  private final String name;
  private final String certificate;
  private final Optional<String> url;
  private final Boolean isChecked;
  private final Boolean isValid;

  private final List<Organization> hasTrust;
  private final List<Token> hasIssued;

  public TrustedParty(
      UUID id,
      String name,
      String certificate,
      String url,
      Boolean isChecked,
      Boolean isValid,
      List<Organization> hasTrust,
      List<Token> hasIssued) {
    this.id = Optional.ofNullable(id);
    this.name = name;
    this.certificate = certificate;
    this.url = Optional.ofNullable(url);
    this.isChecked = isChecked;
    this.isValid = isValid;
    this.hasTrust = hasTrust;
    this.hasIssued = hasIssued;
  }

  public TrustedParty(UUID id, String name, String certificate, String url) {
    this.id = Optional.ofNullable(id);
    this.name = this.certificate = certificate;
    this.url = Optional.ofNullable(url);
    this.isChecked = false;
    this.isValid = false;
    this.hasTrust = Collections.emptyList();
    this.hasIssued = Collections.emptyList();
  }

  public TrustedParty(String name, String certificate) {
    this.id = Optional.empty();
    this.name = this.certificate = certificate;
    this.url = Optional.empty();
    this.isChecked = false;
    this.isValid = false;
    this.hasTrust = Collections.emptyList();
    this.hasIssued = Collections.emptyList();
  }

  public TrustedParty withId(UUID id) {
    return new TrustedParty(
        id,
        this.getName(),
        this.getCertificate(),
        this.getUrl().orElse(null),
        this.getIsChecked(),
        this.getIsValid(),
        this.getHasTrust(),
        this.getHasIssued());
  }

  public TrustedParty withUrl(String url) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getName(),
        this.getCertificate(),
        url,
        this.getIsChecked(),
        this.getIsValid(),
        this.getHasTrust(),
        this.getHasIssued());
  }

  public TrustedParty withIsChecked(Boolean isChecked) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getName(),
        this.getCertificate(),
        this.getUrl().orElse(null),
        isChecked,
        this.getIsValid(),
        this.getHasTrust(),
        this.getHasIssued());
  }

  public TrustedParty withIsValid(Boolean isValid) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getName(),
        this.getCertificate(),
        this.getUrl().orElse(null),
        this.getIsChecked(),
        isValid,
        this.getHasTrust(),
        this.getHasIssued());
  }

  public TrustedParty withHasTrust(Organization organization) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getName(),
        this.getCertificate(),
        this.getUrl().orElse(null),
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
        this.getName(),
        this.getCertificate(),
        this.getUrl().orElse(null),
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

  public String getName() {
    return name;
  }

  public String getCertificate() {
    return certificate;
  }

  public Optional<String> getUrl() {
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
