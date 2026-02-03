package org.commonprovenance.framework.store.model;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class Token {
  private final Optional<UUID> id;
  private final Optional<Document> document;
  private final String hash;
  private final Optional<HashFunction> hashFunction;
  private final String signature;
  private final Optional<ZonedDateTime> created;

  public Token(UUID id,
      Document document,
      String hash,
      HashFunction hashFunction,
      String signature,
      ZonedDateTime created) {
    this.id = Optional.ofNullable(id);
    this.document = Optional.ofNullable(document);
    this.hash = hash;
    this.hashFunction = Optional.ofNullable(hashFunction);
    this.signature = signature;
    this.created = Optional.ofNullable(created);
  }

  public Token withId(UUID id) {
    return new Token(
        id,
        this.getDocument().orElse(null),
        this.getHash(),
        this.getHashFunction().orElse(null),
        this.getSignature(),
        this.getCreated().orElse(null));
  }

  public Token withGeneratedId() {
    return this.withId(this.getId().orElse(UUID.randomUUID()));
  }

  public Token withDocument(Document document) {
    return new Token(
        this.getId().orElse(null),
        document,
        this.getHash(),
        this.getHashFunction().orElse(null),
        this.getSignature(),
        this.getCreated().orElse(null));
  }

  public Token withHashFunction(HashFunction hashFunction) {
    return new Token(
        this.getId().orElse(null),
        this.getDocument().orElse(null),
        this.getHash(),
        hashFunction,
        this.getSignature(),
        this.getCreated().orElse(null));
  }

  public Token withCreated(ZonedDateTime created) {
    return new Token(
        this.getId().orElse(null),
        this.getDocument().orElse(null),
        this.getHash(),
        this.getHashFunction().orElse(null),
        this.getSignature(),
        created);
  }

  public Optional<UUID> getId() {
    return id;
  }

  public Optional<Document> getDocument() {
    return document;
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

  public Optional<ZonedDateTime> getCreated() {
    return created;
  }
}
