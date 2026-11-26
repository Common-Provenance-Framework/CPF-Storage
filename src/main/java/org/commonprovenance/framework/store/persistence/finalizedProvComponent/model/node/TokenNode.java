package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.WasIssuedBy;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasId;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasJwtToken;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasTrustedPartyNodes;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Token")
public record TokenNode(
    @Id @GeneratedValue String id,
    String jwt,
    @Relationship(type = "was_issued_by", direction = Relationship.Direction.OUTGOING) List<WasIssuedBy> wasIssuedBy) implements
    HasId,
    HasJwtToken,
    HasTrustedPartyNodes {
  @PersistenceCreator
  public TokenNode {
  }

  public TokenNode(String jwt) {
    this(null, jwt, Collections.emptyList());
  }

  // Factory methods
  public TokenNode withJwt(String jwtToken) {
    return new TokenNode(
        this.id(),
        jwtToken,
        this.wasIssuedBy());
  }

  public TokenNode withTrustedParty(Optional<TrustedPartyNode> maybeTrustedPartyNode) {
    return maybeTrustedPartyNode.map(this::withTrustedParty).orElse(this);
  }

  public TokenNode withTrustedParty(TrustedPartyNode trustedPartyNode) {
    if (trustedPartyNode == null) {
      return this;
    }

    List<WasIssuedBy> updatedWasIssuedBy = Stream.concat(
        this.wasIssuedBy().stream(),
        Stream.of(new WasIssuedBy(trustedPartyNode)))
        .collect(Collectors.toList());

    return new TokenNode(
        this.id(),
        this.jwt(),
        updatedWasIssuedBy);
  }

  // Wither method for Neo4j to set relationships
  public TokenNode withWasIssuedBy(List<WasIssuedBy> wasIssuedBy) {
    return new TokenNode(
        this.id(),
        this.jwt(),
        wasIssuedBy);
  }

  @Override
  public List<TrustedPartyNode> trustedParties() {
    return this.wasIssuedBy().stream()
        .map(WasIssuedBy::trustedParty)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "TokenNode [id=" + this.id
        + ", jwt=" + this.jwt
        + ", wasIssuedBy=" + this.wasIssuedBy + "]";
  }

}
