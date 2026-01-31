package org.commonprovenance.framework.store.model;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Token {
  private final UUID id;
  private final Document document;
  private final String hash;
  private final HashFunction hashFunction;
  private final String signature;
  private final ZonedDateTime created;

  public Token(UUID id,
      Document document,
      String hash,
      HashFunction hashFunction,
      String signature,
      ZonedDateTime created) {
    this.id = id;
    this.document = document;
    this.hash = hash;
    this.hashFunction = hashFunction;
    this.signature = signature;
    this.created = created;
  }

  public Token withId(UUID id) {
    return new Token(
        id,
        this.getDocument(),
        this.getHash(),
        this.getHashFunction(),
        this.getSignature(),
        this.getCreated());
  }

  public Token withDocument(Document document) {
    return new Token(
        this.getId(),
        document,
        this.getHash(),
        this.getHashFunction(),
        this.getSignature(),
        this.getCreated());
  }

  public Token withHashFunction(HashFunction hashFunction) {
    return new Token(
        this.getId(),
        this.getDocument(),
        this.getHash(),
        hashFunction,
        this.getSignature(),
        this.getCreated());
  }

  public Token withCreated(ZonedDateTime created) {
    return new Token(
        this.getId(),
        this.getDocument(),
        this.getHash(),
        this.getHashFunction(),
        this.getSignature(),
        created);
  }

  public UUID getId() {
    return id;
  }

  public Document getDocument() {
    return document;
  }

  public String getHash() {
    return hash;
  }

  public HashFunction getHashFunction() {
    return hashFunction;
  }

  public String getSignature() {
    return signature;
  }

  public ZonedDateTime getCreated() {
    return created;
  }

}
