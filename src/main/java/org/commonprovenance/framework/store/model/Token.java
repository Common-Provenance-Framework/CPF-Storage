package org.commonprovenance.framework.store.model;

import java.util.Optional;
import java.util.UUID;

public class Token {
  private final Optional<UUID> id;
  private final String hash;
  private final Optional<HashFunction> hashFunction;
  private final String signature;
  private final Long createdOn;

  public Token(
      UUID id,
      String hash,
      HashFunction hashFunction,
      String signature,
      Long createdOn) {
    this.id = Optional.ofNullable(id);
    this.hash = hash;
    this.hashFunction = Optional.ofNullable(hashFunction);
    this.signature = signature;
    this.createdOn = createdOn;
  }

  public Token withId(UUID id) {
    return new Token(
        id,
        this.getHash(),
        this.getHashFunction().orElse(null),
        this.getSignature(),
        this.getCreatedOn());
  }

  public Token withGeneratedId() {
    return this.withId(this.getId().orElse(UUID.randomUUID()));
  }

  public Token withHashFunction(HashFunction hashFunction) {
    return new Token(
        this.getId().orElse(null),
        this.getHash(),
        hashFunction,
        this.getSignature(),
        this.getCreatedOn());
  }

  public Optional<UUID> getId() {
    return id;
  }

  public String getHash() {
    return hash;
  }

  public Optional<HashFunction> getHashFunction() {
    return hashFunction;
  }

  public String getSignature() {
    return signature;
  }

  public Long getCreatedOn() {
    return createdOn;
  }
}
