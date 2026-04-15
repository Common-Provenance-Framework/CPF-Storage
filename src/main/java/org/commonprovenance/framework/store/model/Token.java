package org.commonprovenance.framework.store.model;

public class Token {
  private final String jwt;

  private final TrustedParty trustedParty;
  private final Document document;

  private final Long createdOn;

  public Token(
      String jwt,
      TrustedParty trustedParty,
      Document document,
      Long createdOn) {
    this.jwt = jwt;
    this.trustedParty = trustedParty;
    this.document = document;
    this.createdOn = createdOn;
  }

  public Token withTrustedParty(TrustedParty trustedParty) {
    return new Token(
        this.getJwt(),
        trustedParty,
        this.getDocument(),
        this.getCreatedOn());
  }

  public Token withDocument(Document document) {
    return new Token(
        this.getJwt(),
        this.getTrustedParty(),
        document,
        this.getCreatedOn());
  }

  public String getJwt() {
    return jwt;
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
