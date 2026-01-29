package org.commonprovenance.framework.store.model;

import java.util.UUID;

public class Document {
  private final UUID identifier;
  private final String graph;
  private final Format format;

  public Document(UUID identifier, String graph, Format format) {
    this.identifier = identifier;
    this.graph = graph;
    this.format = format;
  }

  public UUID getIdentifier() {
    return identifier;
  }

  public String getGraph() {
    return graph;
  }

  public Format getFormat() {
    return format;
  }
}
