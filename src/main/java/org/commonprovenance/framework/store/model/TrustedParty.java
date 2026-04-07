package org.commonprovenance.framework.store.model;

import java.util.Optional;

public class TrustedParty {
  private final Optional<String> id;
  private final String name;
  private final String certificate;
  private final Optional<String> url;
  private final Boolean isChecked;
  private final Boolean isValid;
  private final Boolean isDefault;

  public TrustedParty(
      String id,
      String name,
      String certificate,
      String url,
      Boolean isChecked,
      Boolean isValid,
      Boolean isDefault) {
    this.id = Optional.ofNullable(id);
    this.name = name;
    this.certificate = certificate;
    this.url = Optional.ofNullable(url);
    this.isChecked = isChecked;
    this.isValid = isValid;
    this.isDefault = isDefault;
  }

  public TrustedParty(
      String name,
      String certificate,
      String url,
      Boolean isChecked,
      Boolean isValid,
      Boolean isDefault) {
    this.id = Optional.empty();
    this.name = name;
    this.certificate = certificate;
    this.url = Optional.ofNullable(url);
    this.isChecked = isChecked;
    this.isValid = isValid;
    this.isDefault = isDefault;
  }

  public TrustedParty(
      String id,
      String name,
      String certificate,
      String url,
      Boolean isDefault) {
    this.id = Optional.ofNullable(id);
    this.name = name;
    this.certificate = certificate;
    this.url = Optional.ofNullable(url);
    this.isChecked = false;
    this.isValid = false;
    this.isDefault = isDefault;
  }

  public TrustedParty(String name, String certificate) {
    this.id = Optional.empty();
    this.name = name;
    this.certificate = certificate;
    this.url = Optional.empty();
    this.isChecked = false;
    this.isValid = false;
    this.isDefault = false;
  }

  public TrustedParty withId(String id) {
    return new TrustedParty(
        id,
        this.getName(),
        this.getCertificate(),
        this.getUrl().orElse(null),
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedParty withUrl(String url) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getName(),
        this.getCertificate(),
        url,
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedParty withIsChecked(Boolean isChecked) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getName(),
        this.getCertificate(),
        this.getUrl().orElse(null),
        isChecked,
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedParty withIsValid(Boolean isValid) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getName(),
        this.getCertificate(),
        this.getUrl().orElse(null),
        this.getIsChecked(),
        isValid,
        this.getIsDefault());
  }

  public TrustedParty withIsDefault(Boolean isDefault) {
    return new TrustedParty(
        this.getId().orElse(null),
        this.getName(),
        this.getCertificate(),
        this.getUrl().orElse(null),
        this.getIsChecked(),
        this.getIsValid(),
        isDefault);
  }

  public Optional<String> getId() {
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

  public Boolean getIsDefault() {
    return isDefault;
  }

}
