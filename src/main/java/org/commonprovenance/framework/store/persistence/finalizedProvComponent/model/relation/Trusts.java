package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public record Trusts(
    @Id @GeneratedValue String id,
    @TargetNode TrustedPartyNode trustedParty) {
  @PersistenceCreator
  public Trusts {
  }

  public Trusts(TrustedPartyNode trustedParty) {
    this(null, trustedParty);
  }

  public Trusts withTrustedParty(TrustedPartyNode trustedParty) {
    return new Trusts(id, trustedParty);
  }

  @Override
  public String toString() {
    return "Trusts [trustedParty=" + this.trustedParty + "]";
  }

}
