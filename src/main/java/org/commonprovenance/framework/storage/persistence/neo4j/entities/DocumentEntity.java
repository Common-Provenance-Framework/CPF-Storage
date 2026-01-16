package org.commonprovenance.framework.storage.persistence.neo4j.entities;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Document")
public class DocumentEntity {
  @Id
  private final String identifier;
  private final String graph;
  private final String format;

  public DocumentEntity(String identifier, String graph, String format) {
    this.identifier = identifier;
    this.graph = graph;
    this.format = format;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public String getGraph() {
    return this.graph;
  }

  public String getFormat() {
    return this.format;
  }
}
