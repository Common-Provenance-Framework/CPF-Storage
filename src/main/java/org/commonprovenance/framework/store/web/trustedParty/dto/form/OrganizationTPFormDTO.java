package org.commonprovenance.framework.store.web.trustedParty.dto.form;

import java.util.List;

public class OrganizationTPFormDTO {
  private final String name;
  private final String clientCertificate;
  private final List<String> intermediateCertificates;

  public OrganizationTPFormDTO(
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
