package org.commonprovenance.framework.store.model;

import java.util.Optional;

import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasIsChecked;
import org.commonprovenance.framework.store.common.dto.HasIsDefault;
import org.commonprovenance.framework.store.common.dto.HasIsValid;
import org.commonprovenance.framework.store.common.dto.HasName;
import org.commonprovenance.framework.store.common.dto.HasUrlOptional;
import org.commonprovenance.framework.store.common.validation.ValidatableDTO;

public class TrustedParty extends ValidatableDTO implements
    HasName<TrustedParty>,
    HasClientCertificate<TrustedParty>,
    HasUrlOptional<TrustedParty>,
    HasIsChecked<TrustedParty>,
    HasIsValid<TrustedParty>,
    HasIsDefault<TrustedParty> {
  private final String name;
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
      Boolean isDefault) {
    this.name = name;
    this.certificate = certificate;
    this.url = Optional.ofNullable(url);
    this.isChecked = false;
    this.isValid = false;
    this.isDefault = isDefault;
  }

  public TrustedParty(String name, String certificate) {
    this.name = name;
    this.certificate = certificate;
    this.url = Optional.empty();
    this.isChecked = false;
    this.isValid = false;
    this.isDefault = false;
  }

  public TrustedParty() {
    this.name = null;
    this.certificate = null;
    this.url = Optional.empty();
    this.isChecked = false;
    this.isValid = false;
    this.isDefault = false;

  }

  public TrustedParty withName(String name) {
    return new TrustedParty(
        name,
        this.getClientCertificate(),
        this.getUrl().orElse(null),
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedParty withClientCertificate(String certificate) {
    return new TrustedParty(
        this.getName(),
        certificate,
        this.getUrl().orElse(null),
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedParty withUrl(String url) {
    return new TrustedParty(
        this.getName(),
        this.getClientCertificate(),
        url,
        this.getIsChecked(),
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedParty withIsChecked(Boolean isChecked) {
    return new TrustedParty(
        this.getName(),
        this.getClientCertificate(),
        this.getUrl().orElse(null),
        isChecked,
        this.getIsValid(),
        this.getIsDefault());
  }

  public TrustedParty withIsValid(Boolean isValid) {
    return new TrustedParty(
        this.getName(),
        this.getClientCertificate(),
        this.getUrl().orElse(null),
        this.getIsChecked(),
        isValid,
        this.getIsDefault());
  }

  public TrustedParty withIsDefault(Boolean isDefault) {
    return new TrustedParty(
        this.getName(),
        this.getClientCertificate(),
        this.getUrl().orElse(null),
        isDefault || this.getIsChecked(),
        isDefault || this.getIsValid(),
        isDefault);
  }

  public String getName() {
    return name;
  }

  public String getClientCertificate() {
    return certificate;
  }

  public Optional<String> getUrl() {
    return url;
  }

  public Optional<String> getUrlIfNotDefault() {
    return isDefault
        ? Optional.empty()
        : url;
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

  @Override
  public String toString() {
    return "TrustedParty [name=" + name + ", certificate=" + certificate + ", url=" + url + ", isChecked=" + isChecked + ", isValid=" + isValid + ", isDefault=" + isDefault + "]";
  }

}
