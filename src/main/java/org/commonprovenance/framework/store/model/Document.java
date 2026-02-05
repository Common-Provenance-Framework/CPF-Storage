package org.commonprovenance.framework.store.model;

import java.util.Optional;
import java.util.UUID;

public class Document {
  private final Optional<UUID> id;
  private final String graph;
  private final Optional<Format> format;

  public Document(UUID id, String graph, Format format) {
    this.id = Optional.ofNullable(id);
    this.graph = graph;
    this.format = Optional.ofNullable(format);
  }

  public Document withId(UUID id) {
    return new Document(
        id,
        this.getGraph(),
        this.getFormat().orElse(null));
  }

  public Document withFormat(Format format) {
    return new Document(
        this.getId().orElse(null),
        this.getGraph(),
        format);
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
}
