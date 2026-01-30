package org.commonprovenance.framework.store.web.trustedParty.dto.response;

public class TokenResponseDTO implements HasId, HasHashFunction, HasCreated {
  private final String id;
  private final DocumentResponseDTO document;
  private final String hash;
  private final String hashFunction;
  private final String signature;
  private final String created;

  public TokenResponseDTO(
      String id,
      DocumentResponseDTO document,
      String hash,
      String hashFunction,
      String signature,
      String created) {
    this.id = id;
    this.document = document;
    this.hash = hash;
    this.hashFunction = hashFunction;
    this.signature = signature;
    this.created = created;
  }

  public String getId() {
    return id;
  }

  public DocumentResponseDTO getDocument() {
    return document;
  }

  public String getHash() {
    return hash;
  }

  public String getHashFunction() {
    return hashFunction;
  }

  public String getSignature() {
    return signature;
  }

  public String getCreated() {
    return created;
  }
}
