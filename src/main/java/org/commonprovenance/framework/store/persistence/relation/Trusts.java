package org.commonprovenance.framework.store.persistence.relation;

import org.commonprovenance.framework.store.persistence.entity.TrustedPartyEntity;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import jakarta.validation.constraints.NotNull;

@RelationshipProperties
public final class Trusts {

  @Id
  @GeneratedValue
  private final Long id;

  @TargetNode
  private final TrustedPartyEntity trustedParty;

  // Constructor for full initialization (used by Neo4j when reading and writing)
  public Trusts(Long id, TrustedPartyEntity trustedParty) {
    this.id = id;
    this.trustedParty = trustedParty;
  }

  // Constructor for creating new relationships (id will be generated)
  public Trusts(TrustedPartyEntity trustedParty) {
    this(null, trustedParty);
  }

  // Wither methods for immutability
  public @NotNull Trusts withId(Long id) {
    return new Trusts(id, this.getTrustedParty());
  }

  public @NotNull Trusts withTrustedParty(@NotNull TrustedPartyEntity trustedParty) {
    return new Trusts(this.getId(), trustedParty);
  }

  // Getters

  public Long getId() {
    return this.id;
  }

  public TrustedPartyEntity getTrustedParty() {
    return this.trustedParty;
  }
}
