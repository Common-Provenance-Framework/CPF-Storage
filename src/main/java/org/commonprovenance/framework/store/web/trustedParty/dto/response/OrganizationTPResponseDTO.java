package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasIdentifier;

public class OrganizationTPResponseDTO implements
    HasIdentifier<OrganizationTPResponseDTO>,
    HasClientCertificate<OrganizationTPResponseDTO> {
  private final String id;
  private final String certificate;

  public OrganizationTPResponseDTO(
      String id,
      String certificate) {
    this.id = id;
    this.certificate = certificate;
  }

  @Override
  public OrganizationTPResponseDTO withIdentifier(String identifier) {
    return new OrganizationTPResponseDTO(
        identifier,
        this.getClientCertificate());
  }

  @Override
  public OrganizationTPResponseDTO withClientCertificate(String clientCertificate) {
    return new OrganizationTPResponseDTO(
        this.getIdentifier(),
        clientCertificate);
  }

  @Override
  public String getIdentifier() {
    return this.id;
  }

  @Override
  public String getClientCertificate() {
    return this.certificate;
  }

}
