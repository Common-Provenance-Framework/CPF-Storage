package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public record HasToken(
    @Id @GeneratedValue String id,
    @TargetNode TokenNode token) {
  @PersistenceCreator
  public HasToken {
  }

  public HasToken(TokenNode token) {
    this(null, token);
  }

  public HasToken withToken(TokenNode token) {
    return new HasToken(id, token);
  }

  @Override
  public String toString() {
    return "HasToken [token=" + this.token + "]";
  }

}
