package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasIdentifier;

public class CertificateTPResponseDTO implements
    HasIdentifier<CertificateTPResponseDTO>,
    HasClientCertificate<CertificateTPResponseDTO> {
  private final String id; // Organization identifier here
  private final String certificate;

  public CertificateTPResponseDTO(
      String id,
      String certificate) {
    this.id = id;
    this.certificate = certificate;
  }

  @Override
  public CertificateTPResponseDTO withIdentifier(String name) {
    return new CertificateTPResponseDTO(
        name,
        this.getClientCertificate());
  }

  @Override
  public CertificateTPResponseDTO withClientCertificate(String clientCertificate) {
    return new CertificateTPResponseDTO(
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
