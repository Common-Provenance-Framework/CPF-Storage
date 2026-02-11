package org.commonprovenance.framework.store.model;

import java.util.Optional;
import java.util.UUID;

public class Document {
  private final Optional<UUID> id;
  private final String graph;
  private final Optional<Format> format;
  private final String signature;

  public Document(UUID id, String graph, Format format, String signature) {
    this.id = Optional.ofNullable(id);
    this.graph = graph;
    this.format = Optional.ofNullable(format);
    this.signature = signature;
  }

  public Document withId(UUID id) {
    return new Document(
        id,
        this.getGraph(),
        this.getFormat().orElse(null),
        this.getSignature());
  }

  public Document withFormat(Format format) {
    return new Document(
        this.getId().orElse(null),
        this.getGraph(),
        format,
        this.getSignature());
  }

  public Optional<UUID> getId() {
    return id;
  }

  public String getGraph() {
    return graph;
  }

  public Optional<Format> getFormat() {
    return format;
  }

  public String getSignature() {
    return signature;
  }
}
