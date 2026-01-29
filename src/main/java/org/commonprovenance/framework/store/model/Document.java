package org.commonprovenance.framework.store.model;

import java.util.UUID;

public class Document {
  private final UUID id;
  private final String graph;
  private final Format format;

  public Document(UUID id, String graph, Format format) {
    this.id = id;
    this.graph = graph;
    this.format = format;
  }

  public UUID getId() {
    return id;
  }

  public String getGraph() {
    return graph;
  }

  public Format getFormat() {
    return format;
  }
}
