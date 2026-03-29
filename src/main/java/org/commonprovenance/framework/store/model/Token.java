package org.commonprovenance.framework.store.model;

import java.util.Optional;

public class Token {
  private final Optional<String> id;
  private final String hash;
  private final String signature;

  private final AdditionalData additionalData;
  private final TrustedParty trustedParty;
  private final Document document;

  private final Long createdOn;

  public Token(
      String id,
      String hash,
      String signature,
      AdditionalData additionalData,
      TrustedParty trustedParty,
      Document document,
      Long createdOn) {
    this.id = Optional.ofNullable(id);
    this.hash = hash;
    this.signature = signature;
    this.additionalData = additionalData;
    this.trustedParty = trustedParty;
    this.document = document;
    this.createdOn = createdOn;
  }

  public Token withId(String id) {
    return new Token(
        id,
        this.getHash(),
        this.getSignature(),
        this.getAdditionalData(),
        this.getTrustedParty(),
        this.getDocument(),
        this.getCreatedOn());
  }

  public Token withTrustedParty(TrustedParty trustedParty) {
    return new Token(
        this.getId().orElse(null),
        this.getHash(),
        this.getSignature(),
        this.getAdditionalData(),
        trustedParty,
        this.getDocument(),
        this.getCreatedOn());
  }

  public Token withDocument(Document document) {
    return new Token(
        this.getId().orElse(null),
        this.getHash(),
        this.getSignature(),
        this.getAdditionalData(),
        this.getTrustedParty(),
        document,
        this.getCreatedOn());
  }

  public Optional<String> getId() {
    return id;
  }

  public String getHash() {
    return hash;
  }

  public String getSignature() {
    return signature;
  }

  public AdditionalData getAdditionalData() {
    return additionalData;
  }

  public TrustedParty getTrustedParty() {
    return trustedParty;
  }

  public Document getDocument() {
    return document;
  }

  public Long getCreatedOn() {
    return createdOn;
  }

}
