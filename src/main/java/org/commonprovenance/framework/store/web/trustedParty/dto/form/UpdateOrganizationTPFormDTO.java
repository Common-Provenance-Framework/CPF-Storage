package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.util.List;

import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasIntermediateCertificates;
import org.commonprovenance.framework.store.common.validation.ValidatableDTO;

public class UpdateOrganizationTPFormDTO extends ValidatableDTO implements
    HasClientCertificate<UpdateOrganizationTPFormDTO>,
    HasIntermediateCertificates<UpdateOrganizationTPFormDTO> {
  private final String clientCertificate;
  private final List<String> intermediateCertificates;

  public UpdateOrganizationTPFormDTO() {
    this.clientCertificate = null;
    this.intermediateCertificates = null;
  }

  public UpdateOrganizationTPFormDTO(
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
  }

  @Override
  public UpdateOrganizationTPFormDTO withClientCertificate(String clientCertificate) {
    return new UpdateOrganizationTPFormDTO(
        clientCertificate,
        this.getIntermediateCertificates());
  }

  @Override
  public UpdateOrganizationTPFormDTO withIntermediateCertificates(List<String> intermediateCertificates) {
    return new UpdateOrganizationTPFormDTO(
        this.getClientCertificate(),
        intermediateCertificates);
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
