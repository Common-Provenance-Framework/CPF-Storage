package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.util.List;

import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.common.validation.ValidatableDTO;

public class RegisterOrganizationTPFormDTO extends ValidatableDTO
    implements HasOrganizationId<RegisterOrganizationTPFormDTO> {
  private final String organizationId;
  private final String clientCertificate;
  private final List<String> intermediateCertificates;

  public RegisterOrganizationTPFormDTO() {
    this.organizationId = null;
    this.clientCertificate = null;
    this.intermediateCertificates = null;
  }

  public RegisterOrganizationTPFormDTO(
      String organizationId,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.organizationId = organizationId;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
  }

  @Override
  public RegisterOrganizationTPFormDTO withOrganizationId(String id) {
    return new RegisterOrganizationTPFormDTO(
        id,
        this.getClientCertificate(),
        this.getIntermediateCertificates());
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public String getClientCertificate() {
    return clientCertificate;
  }

  public List<String> getIntermediateCertificates() {
    return intermediateCertificates;
  }

}
