package org.commonprovenance.framework.store.model;

import java.util.Optional;

import org.commonprovenance.framework.store.common.dto.HasCreatedOn;
import org.commonprovenance.framework.store.common.dto.HasJwtToken;
import org.commonprovenance.framework.store.common.dto.HasTrustedPartyOptional;

public class Token implements
    HasJwtToken<Token>,
    HasTrustedPartyOptional<Token>,
    HasCreatedOn<Token> {
  private final String jwt;

  private final Optional<TrustedParty> trustedParty;

  private final Long createdOn;

  public Token(
      String jwt,
      TrustedParty trustedParty,
      Long createdOn) {
    this.jwt = jwt;
    this.trustedParty = Optional.ofNullable(trustedParty);
    this.createdOn = createdOn;
  }

  public Token() {
    this.jwt = null;
    this.trustedParty = Optional.empty();
    this.createdOn = 0L;
  }

  public Token withJwt(String jwtToken) {
    return new Token(
        jwtToken,
        this.getTrustedParty().orElse(null),
        this.getCreatedOn());
  }

  public Token withTrustedParty(TrustedParty trustedParty) {
    return new Token(
        this.getJwt(),
        trustedParty,
        this.getCreatedOn());
  }

  public Token withTrustedParty(Optional<TrustedParty> maybeTrustedParty) {
    return maybeTrustedParty
        .map(trustedParty -> new Token(
            this.getJwt(),
            trustedParty,
            this.getCreatedOn()))
        .orElse(this);
  }

  public Token withCreatedOn(Long createdOn) {
    return new Token(
        this.getJwt(),
        this.getTrustedParty().orElse(null),
        createdOn);
  }

  public String getJwt() {
    return jwt;
  }

  public Optional<TrustedParty> getTrustedParty() {
    return trustedParty;
  }

  public Long getCreatedOn() {
    return createdOn;
  }

}
