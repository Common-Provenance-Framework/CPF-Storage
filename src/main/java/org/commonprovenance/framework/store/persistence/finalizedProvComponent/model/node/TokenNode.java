package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.common.dto.HasId;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.BelongsTo;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.WasIssuedBy;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Token")
public class TokenNode implements HasId {

  @Id
  @GeneratedValue
  private final String id;

  private final String jwt;

  @Relationship(type = "was_issued_by", direction = Relationship.Direction.OUTGOING)
  private final List<WasIssuedBy> wasIssuedBy;

  @Relationship(type = "belongs_to", direction = Relationship.Direction.OUTGOING)
  private final List<BelongsTo> belongsTo;

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public TokenNode(
      String id,
      String jwt,
      List<WasIssuedBy> wasIssuedBy,
      List<BelongsTo> belongsTo) {
    this.id = id;
    this.jwt = jwt;
    this.wasIssuedBy = wasIssuedBy;
    this.belongsTo = belongsTo;
  }

  // Constructor for creating new node (id will be generated)
  public TokenNode(String jwt) {
    this.id = null;
    this.jwt = jwt;
    this.wasIssuedBy = Collections.emptyList();
    this.belongsTo = Collections.emptyList();
  }

  // Factory methods
  public TokenNode withTrustedParty(TrustedPartyNode trustedPartyEntity) {
    if (trustedPartyEntity == null) {
      return this;
    }

    List<WasIssuedBy> updatedWasIssuedBy = Stream.concat(
        this.getWasIssuedBy().stream(),
        Stream.of(new WasIssuedBy(trustedPartyEntity)))
        .collect(Collectors.toList());

    return new TokenNode(
        this.getId(),
        this.getJwt(),
        updatedWasIssuedBy,
        this.getBelongsTo());
  }

  // Wither method for Neo4j to set relationships
  public TokenNode withWasIssuedBy(List<WasIssuedBy> wasIssuedBy) {
    return new TokenNode(
        this.getId(),
        this.getJwt(),
        wasIssuedBy,
        this.getBelongsTo());
  }

  public TokenNode withDocument(DocumentNode documentEntity) {
    if (documentEntity == null) {
      return this;
    }

    List<BelongsTo> updatedBelongsTo = Stream.concat(
        this.getBelongsTo().stream(),
        Stream.of(new BelongsTo(documentEntity)))
        .collect(Collectors.toList());

    return new TokenNode(
        this.getId(),
        this.getJwt(),
        this.getWasIssuedBy(),
        updatedBelongsTo);
  }

  // Wither method for Neo4j to set relationships
  public TokenNode withBelongsTo(List<BelongsTo> belongsTo) {
    return new TokenNode(
        this.getId(),
        this.getJwt(),
        this.getWasIssuedBy(),
        belongsTo);
  }

  public String getId() {
    return this.id;
  }

  public String getJwt() {
    return jwt;
  }

  public List<WasIssuedBy> getWasIssuedBy() {
    return wasIssuedBy;
  }

  public List<BelongsTo> getBelongsTo() {
    return belongsTo;
  }

}
