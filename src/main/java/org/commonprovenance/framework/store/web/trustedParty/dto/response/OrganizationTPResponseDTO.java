package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dto.HasId;

public class OrganizationTPResponseDTO implements HasId {
  private final String id;
  private final String certificate;

  public OrganizationTPResponseDTO(
      String id,
      String certificate) {
    this.id = id;
    this.certificate = certificate;
  }

  public String getId() {
    return id;
  }

  public String getCertificate() {
    return certificate;
  }
}
