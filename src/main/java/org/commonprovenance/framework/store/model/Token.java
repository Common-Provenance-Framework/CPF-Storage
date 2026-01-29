package org.commonprovenance.framework.store.model;

import java.util.Date;
import java.util.UUID;

public class Token {
  private final UUID id;
  private final Document document;
  private final String hash;
  private final HashFunction hashFunction;
  private final String signature;
  private final Date created;

  public Token(UUID id, Document document, String hash, HashFunction hashFunction, String signature, Date created) {
    this.id = id;
    this.document = document;
    this.hash = hash;
    this.hashFunction = hashFunction;
    this.signature = signature;
    this.created = created;
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

  public Date getCreated() {
    return created;
  }

}
