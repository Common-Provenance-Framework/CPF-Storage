package org.commonprovenance.framework.store.controller.dto.form;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Schema(name = "OrganizationForm", description = "Payload used to create or update an organization")
public class OrganizationFormDTO {
  @Schema(description = "Organization identifier", example = "ORG1", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Organization identifier should not be null or empty.")
  private final String identifier;
  @Schema(description = "PEM encoded client certificate", example = "-----BEGIN CERTIFICATE-----...", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "Organization client certificate should not be null or empty.")
  private final String clientCertificate;
  @Schema(description = "PEM encoded intermediate certificates", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "Organization intermediate certificates should not be null.")
  @NotEmpty(message = "Organization intermediate certificates should not be empty.")
  private final List<String> intermediateCertificates;

  @Schema(description = "Trusted party URL used by this organization", example = "http://trustedparty:8080/api/v1/")
  private final String trustedPartyUri;
  @Schema(description = "Clearance period in seconds", example = "3600")
  private final Integer clearancePeriod;

  public OrganizationFormDTO(
      String identifier,
      String clientCertificate,
      List<String> intermediateCertificates,
      String trustedPartyUri,
      Integer clearancePeriod) {
    this.identifier = identifier;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
    this.trustedPartyUri = trustedPartyUri;
    this.clearancePeriod = clearancePeriod;
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

  public String getTrustedPartyUri() {
    return trustedPartyUri;
  }

  public Integer getClearancePeriod() {
    return clearancePeriod;
  }

}
