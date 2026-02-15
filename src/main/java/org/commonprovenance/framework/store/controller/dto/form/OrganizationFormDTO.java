package org.commonprovenance.framework.store.controller.dto.form;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class OrganizationFormDTO {
  @NotBlank(message = "Organization name should not be null or empty.")
  private final String name;
  @NotBlank(message = "Organization client certificate should not be null or empty.")
  private final String clientCertificate;
  @NotNull(message = "Organization intermediate certificates should not be null.")
  @NotEmpty(message = "Organization intermediate certificates should not be empty.")
  private final List<String> intermediateCertificates;

  private final String trustedPartyUri;
  private final Integer clearancePeriod;

  public OrganizationFormDTO(
      String name,
      String clientCertificate,
      List<String> intermediateCertificates,
      String trustedPartyUri,
      Integer clearancePeriod) {
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
    this.trustedPartyUri = trustedPartyUri;
    this.clearancePeriod = clearancePeriod;
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

  public String getTrustedPartyUri() {
    return trustedPartyUri;
  }

  public Integer getClearancePeriod() {
    return clearancePeriod;
  }

}
