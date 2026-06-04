package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.HasToken;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasFormat;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasGraph;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasId;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIdentifier;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasTokenNodes;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Document")
public class DocumentNode implements
    HasId,
    HasIdentifier,
    HasGraph,
    HasFormat,
    HasTokenNodes {
  @Id
  @GeneratedValue
  private final String id;

  private final String identifier;
  private final String graph;
  private final String format;

  @Relationship(type = "has_token", direction = Relationship.Direction.OUTGOING)
  private final List<HasToken> hasToken;

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public DocumentNode(
      String id,
      String identifier,
      String graph,
      String format,
      List<HasToken> hasToken) {
    this.id = id;
    this.identifier = identifier;
    this.graph = graph;
    this.format = format;

    this.hasToken = hasToken;
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

    this.hasToken = Collections.emptyList();
  }

  public DocumentNode(String graph) {
    this.id = null;
    this.identifier = null;
    this.graph = graph;
    this.format = null;

    this.hasToken = Collections.emptyList();
  }

  // Factory methods
  public DocumentNode withIdentifier(String identifier) {
    return new DocumentNode(
        this.getId(),
        identifier,
        this.getGraph(),
        this.getFormat(),
        this.getHasToken());
  }

  public DocumentNode withGraph(String graph) {
    return new DocumentNode(
        this.getId(),
        this.getIdentifier(),
        graph,
        this.getFormat(),
        this.getHasToken());
  }

  public DocumentNode withDocumentFormat(Format format) {
    return new DocumentNode(
        this.getId(),
        this.getIdentifier(),
        this.getGraph(),
        format.toString(),
        this.getHasToken());
  }

  public DocumentNode withToken(Optional<TokenNode> maybeTokenNode) {
    return maybeTokenNode.map(this::withToken).orElse(this);
  }

  public DocumentNode withToken(TokenNode tokenNode) {
    if (tokenNode == null) {
      return this;
    }

    List<HasToken> updatedHasToken = Stream.concat(
        this.getHasToken().stream(),
        Stream.of(new HasToken(tokenNode)))
        .collect(Collectors.toList());

    return new DocumentNode(
        this.getId(),
        this.getIdentifier(),
        this.getGraph(),
        this.getFormat(),
        updatedHasToken);
  }

  // Wither method for Neo4j to set relationships
  public DocumentNode withHasToken(List<HasToken> hasToken) {
    return new DocumentNode(
        this.getId(),
        this.getIdentifier(),
        this.getGraph(),
        this.getFormat(),
        hasToken);
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getIdentifier() {
    return this.identifier;
  }

  @Override
  public String getGraph() {
    return this.graph;
  }

  @Override
  public String getFormat() {
    return this.format;
  }

  public List<HasToken> getHasToken() {
    return hasToken;
  }

  @Override
  public List<TokenNode> getTokens() {
    return this.getHasToken().stream()
        .map(HasToken::getToken)
        .collect(Collectors.toList());
  }

}
