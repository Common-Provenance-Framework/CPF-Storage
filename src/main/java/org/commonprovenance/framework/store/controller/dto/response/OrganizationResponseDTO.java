package org.commonprovenance.framework.store.controller.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "OrganizationResponse", description = "Organization details")
public class OrganizationResponseDTO {

  @Schema(description = "Organization identifier", example = "ORG1")
  private final String identifier;
  @Schema(description = "PEM encoded client certificate", example = "-----BEGIN CERTIFICATE-----...")
  private final String clientCertificate;
  @Schema(description = "PEM encoded intermediate certificates")
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
