package org.commonprovenance.framework.store.model;

import java.util.Optional;

import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasId;
import org.commonprovenance.framework.store.common.dto.HasIsChecked;
import org.commonprovenance.framework.store.common.dto.HasIsDefault;
import org.commonprovenance.framework.store.common.dto.HasIsValid;
import org.commonprovenance.framework.store.common.dto.HasUrlOptional;
import org.commonprovenance.framework.store.common.validation.DTOValidator;

public class TrustedParty extends DTOValidator implements
    HasId<TrustedParty>,
    HasClientCertificate<TrustedParty>,
    HasUrlOptional<TrustedParty>,
    HasIsChecked<TrustedParty>,
    HasIsValid<TrustedParty>,
    HasIsDefault<TrustedParty> {
  private final String id;
  private final String certificate;
  private final Optional<String> url;
  private final Boolean isChecked;
  private final Boolean isValid;
  private final Boolean isDefault;

  public TrustedParty(
      String name,
      String certificate,
      String url,
      Boolean isChecked,
      Boolean isValid,
      Boolean isDefault) {
    this.id = name;
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
      Boolean isDefault) {
    this.id = name;
    this.certificate = certificate;
    this.url = Optional.ofNullable(url);
    this.isChecked = false;
    this.isValid = false;
    this.isDefault = isDefault;
  }

  public TrustedParty(String name, String certificate) {
    this.id = name;
    this.certificate = certificate;
    this.url = Optional.empty();
    this.isChecked = false;
    this.isValid = false;
    this.isDefault = false;
  }

  public TrustedParty() {
    this.id = null;
    this.certificate = null;
    this.url = Optional.empty();
    this.isChecked = false;
    this.isValid = false;
    this.isDefault = false;

  }

  @Override
  public TrustedParty withId(String id) {
    return new TrustedParty(
        id,
        this.getClientCertificate(),
        this.getUrl().orElse(null),
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  @Override
  public TrustedParty withClientCertificate(String certificate) {
    return new TrustedParty(
        this.getId(),
        certificate,
        this.getUrl().orElse(null),
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  @Override
  public TrustedParty withUrl(String url) {
    return new TrustedParty(
        this.getId(),
        this.getClientCertificate(),
        url,
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  @Override
  public TrustedParty withIsChecked(Boolean isChecked) {
    return new TrustedParty(
        this.getId(),
        this.getClientCertificate(),
        this.getUrl().orElse(null),
        isChecked,
        this.getIsValid(),
        this.getIsDefault());
  }

  @Override
  public TrustedParty withIsValid(Boolean isValid) {
    return new TrustedParty(
        this.getId(),
        this.getClientCertificate(),
        this.getUrl().orElse(null),
        this.getIsChecked(),
        isValid,
        this.getIsDefault());
  }

  @Override
  public TrustedParty withIsDefault(Boolean isDefault) {
    return new TrustedParty(
        this.getId(),
        this.getClientCertificate(),
        this.getUrl().orElse(null),
        isDefault || this.getIsChecked(),
        isDefault || this.getIsValid(),
        isDefault);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getClientCertificate() {
    return certificate;
  }

  @Override
  public Optional<String> getUrl() {
    return url;
  }

  public Optional<String> getUrlIfNotDefault() {
    return isDefault
        ? Optional.empty()
        : url;
  }

  @Override
  public Boolean getIsChecked() {
    return isChecked;
  }

  @Override
  public Boolean getIsValid() {
    return isValid;
  }

  @Override
  public Boolean getIsDefault() {
    return isDefault;
  }

  @Override
  public String toString() {
    return "TrustedParty [id=" + id + ", certificate=" + certificate + ", url=" + url + ", isChecked=" + isChecked + ", isValid=" + isValid + ", isDefault=" + isDefault + "]";
  }

}
