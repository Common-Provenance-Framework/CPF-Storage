package org.commonprovenance.framework.store.controller.dto.form;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public class OrganizationFormDTO {
  @NotBlank(message = "Organization name should not be null or empty.")
  private final String name;
  @NotBlank(message = "Organization client certificate should not be null or empty.")
  private final String clientCertificate;
  @NotBlank(message = "Organization intermediate certificates should not be null or empty.")
  private final List<String> intermediateCertificates;

  public OrganizationFormDTO(
      String name,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
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

}
