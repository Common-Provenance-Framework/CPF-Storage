package org.commonprovenance.framework.store.web.trustedParty.dto.response;

public class DocumentTPResponseDTO {
  private final String document;
  private final String signature;

  public DocumentTPResponseDTO(String document, String signature) {
    this.document = document;
    this.signature = signature;
  }

  public String getDocument() {
    return document;
  }

  public String getSignature() {
    return signature;
  }
}
