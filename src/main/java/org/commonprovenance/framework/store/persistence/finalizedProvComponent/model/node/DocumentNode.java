package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.model.GraphFormat;
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
public record DocumentNode(
    @Id @GeneratedValue String id,
    String identifier,
    String graph,
    String format,
    @Relationship(type = "has_token", direction = Relationship.Direction.OUTGOING) List<HasToken> hasToken) implements
    HasId,
    HasIdentifier,
    HasGraph,
    HasFormat,
    HasTokenNodes {
  @PersistenceCreator
  public DocumentNode {
  }

  public DocumentNode(
      String identifier,
      String graph,
      String format) {
    this(null, identifier, graph, format, Collections.emptyList());
  }

  public DocumentNode(String graph) {
    this(null, null, graph, null, Collections.emptyList());
  }

  // Factory methods
  public DocumentNode withIdentifier(String identifier) {
    return new DocumentNode(
        this.id(),
        identifier,
        this.graph(),
        this.format(),
        this.hasToken());
  }

  public DocumentNode withGraph(String graph) {
    return new DocumentNode(
        this.id(),
        this.identifier(),
        graph,
        this.format(),
        this.hasToken());
  }

  public DocumentNode withFormat(GraphFormat format) {
    return new DocumentNode(
        this.id(),
        this.identifier(),
        this.graph(),
        format.toString(),
        this.hasToken());
  }

  public DocumentNode withToken(Optional<TokenNode> maybeTokenNode) {
    return maybeTokenNode.map(this::withToken).orElse(this);
  }

  public DocumentNode withToken(TokenNode tokenNode) {
    if (tokenNode == null) {
      return this;
    }

    List<HasToken> updatedHasToken = Stream.concat(
        this.hasToken().stream(),
        Stream.of(new HasToken(tokenNode)))
        .collect(Collectors.toList());

    return new DocumentNode(
        this.id(),
        this.identifier(),
        this.graph(),
        this.format(),
        updatedHasToken);
  }

  // Wither method for Neo4j to set relationships
  public DocumentNode withHasToken(List<HasToken> hasToken) {
    return new DocumentNode(
        this.id(),
        this.identifier(),
        this.graph(),
        this.format(),
        hasToken);
  }

  @Override
  public List<TokenNode> tokens() {
    return this.hasToken().stream()
        .map(HasToken::token)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "DocumentNode [id=" + this.id
        + ", identifier=" + this.identifier
        + ", graph=" + this.graph
        + ", format=" + this.format
        + ", hasToken=" + this.hasToken + "]";
  }

}
