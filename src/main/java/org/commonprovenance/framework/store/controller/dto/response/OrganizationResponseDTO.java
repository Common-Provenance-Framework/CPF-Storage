package org.commonprovenance.framework.store.controller.dto.response;

import java.util.List;

public class OrganizationResponseDTO {

  private final String identifier;
  private final String clientCertificate;
  private final List<String> intermediateCertificates;

  public OrganizationResponseDTO(
      String identifier,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.identifier = identifier;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
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
}
