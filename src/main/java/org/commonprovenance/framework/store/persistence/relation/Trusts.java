package org.commonprovenance.framework.store.persistence.relation;

import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.entity.TrustedPartyEntity;
import org.commonprovenance.framework.store.persistence.entity.factory.EntityFactory;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import jakarta.validation.constraints.NotNull;

@RelationshipProperties
public final class Trusts {

  @TargetNode
  @Property("trustedParty")
  private final TrustedPartyEntity trustedParty;

  // 1) Constructor used when writing TO Neo4j
  public Trusts(TrustedParty model) {
    this.trustedParty = EntityFactory.toEntity(model).blockOptional().orElse(null);
  }

  // 2) Constructor used when reading FROM Neo4j
  public Trusts(TrustedPartyEntity trustedParty) {
    this.trustedParty = trustedParty;
  }

  public @NotNull Trusts withEntity(@NotNull TrustedPartyEntity trustedParty) {
    return new Trusts(trustedParty);
  }

  // Getters

  public TrustedPartyEntity getTrustedParty() {
    return this.trustedParty;
  }
}
