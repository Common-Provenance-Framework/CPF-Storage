package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.util.List;

import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasIntermediateCertificates;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.common.validation.DTOValidator;

public class RegisterOrganizationTPFormDTO extends DTOValidator implements
    HasOrganizationId<RegisterOrganizationTPFormDTO>,
    HasClientCertificate<RegisterOrganizationTPFormDTO>,
    HasIntermediateCertificates<RegisterOrganizationTPFormDTO> {
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
  public RegisterOrganizationTPFormDTO withOrganizationId(String organizationId) {
    return new RegisterOrganizationTPFormDTO(
        organizationId,
        this.getClientCertificate(),
        this.getIntermediateCertificates());
  }

  @Override
  public RegisterOrganizationTPFormDTO withClientCertificate(String clientCertificate) {
    return new RegisterOrganizationTPFormDTO(
        this.getOrganizationId(),
        clientCertificate,
        this.getIntermediateCertificates());
  }

  @Override
  public RegisterOrganizationTPFormDTO withIntermediateCertificates(List<String> intermediateCertificates) {
    return new RegisterOrganizationTPFormDTO(
        this.getOrganizationId(),
        this.getClientCertificate(),
        intermediateCertificates);
  }

  @Override
  public String getOrganizationId() {
    return organizationId;
  }

  @Override
  public String getClientCertificate() {
    return clientCertificate;
  }

  @Override
  public List<String> getIntermediateCertificates() {
    return intermediateCertificates;
  }

}
