package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasName;

public class TrustedPartyTPResponseDTO implements
    HasName<TrustedPartyTPResponseDTO>,
    HasClientCertificate<TrustedPartyTPResponseDTO> {
  private final String id; // TrustedParty name here
  private final String certificate;

  public TrustedPartyTPResponseDTO(
      String id,
      String certificate) {
    this.id = id;
    this.certificate = certificate;
  }

  @Override
  public TrustedPartyTPResponseDTO withName(String name) {
    return new TrustedPartyTPResponseDTO(
        name,
        this.getClientCertificate());
  }

  @Override
  public TrustedPartyTPResponseDTO withClientCertificate(String clientCertificate) {
    return new TrustedPartyTPResponseDTO(
        this.getName(),
        clientCertificate);
  }

  @Override
  public String getName() {
    return id;
  }

  @Override
  public String getClientCertificate() {
    return certificate;
  }

}
