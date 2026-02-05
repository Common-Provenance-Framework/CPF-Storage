package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dto.HasId;

public class TrustedPartyTPResponseDTO implements HasId {
  private final String id;
  private final String name;
  private final String certificate;

  public TrustedPartyTPResponseDTO(
      String id,
      String name,
      String certificate) {
    this.id = id;
    this.name = name;
    this.certificate = certificate;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCertificate() {
    return certificate;
  }

}
