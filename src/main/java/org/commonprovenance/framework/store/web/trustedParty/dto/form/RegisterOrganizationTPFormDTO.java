package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.util.List;

import org.commonprovenance.framework.store.common.validation.ValidatableDTO;

public class RegisterOrganizationTPFormDTO extends ValidatableDTO {
  private final String name;
  private final String clientCertificate;
  private final List<String> intermediateCertificates;

  public RegisterOrganizationTPFormDTO() {
    this.name = null;
    this.clientCertificate = null;
    this.intermediateCertificates = null;
  }

  public RegisterOrganizationTPFormDTO(
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
