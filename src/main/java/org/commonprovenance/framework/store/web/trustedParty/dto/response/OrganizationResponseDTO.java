package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import java.util.List;

import org.commonprovenance.framework.store.common.dto.HasId;

public class OrganizationResponseDTO implements HasId {
  private final String id;
  private final String name;
  private final String clientCertificate;
  private final List<String> intermediateCertificates;

  public OrganizationResponseDTO(
      String id,
      String name,
      String clientCertificate,
      List<String> intermediateCertificates) {
    this.id = id;
    this.name = name;
    this.clientCertificate = clientCertificate;
    this.intermediateCertificates = intermediateCertificates;
  }

  public String getId() {
    return id;
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
