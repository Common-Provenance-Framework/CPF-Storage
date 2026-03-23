package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import org.commonprovenance.framework.store.common.dto.HasFormat;
import org.commonprovenance.framework.store.common.dto.HasIdentifier;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Document")
public class DocumentNode implements HasIdentifier<DocumentNode>, HasFormat {
  @Id
  @GeneratedValue
  private final String id;

  private final String identifier;
  private final String graph;
  private final String format;

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public DocumentNode(
      String id,
      String identifier,
      String graph,
      String format) {
    this.id = id;
    this.identifier = identifier;
    this.graph = graph;
    this.format = format;
  }

  // Constructor for creating new node (id will be generated)
  public DocumentNode(
      String identifier,
      String graph,
      String format) {
    this.id = null;
    this.identifier = identifier;
    this.graph = graph;
    this.format = format;
  }

  public DocumentNode(String graph) {
    this.id = null;
    this.identifier = null;
    this.graph = graph;
    this.format = null;
  }

  // Factory methods
  public DocumentNode withIdentifier(String identifier) {
    return new DocumentNode(
        identifier,
        this.getGraph(),
        this.getFormat());
  }

  public DocumentNode withFormat(String format) {
    return new DocumentNode(
        this.getIdentifier(),
        this.getGraph(),
        format);
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
