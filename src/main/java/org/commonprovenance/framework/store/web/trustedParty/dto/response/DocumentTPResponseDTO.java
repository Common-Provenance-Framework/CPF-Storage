package org.commonprovenance.framework.store.web.trustedParty.dto.response;

import org.commonprovenance.framework.store.common.dto.HasGraph;

public class DocumentTPResponseDTO implements
    HasGraph<DocumentTPResponseDTO> {
  private final String document;

  public DocumentTPResponseDTO(String document) {
    this.document = document;
  }

  public String getGraph() {
    return document;
  }

}
