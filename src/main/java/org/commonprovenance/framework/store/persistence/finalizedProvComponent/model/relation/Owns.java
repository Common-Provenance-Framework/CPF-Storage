package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
public record Owns(
    @Id @GeneratedValue String id,
    @TargetNode DocumentNode document) {
  @PersistenceCreator
  public Owns {
  }

  public Owns(DocumentNode document) {
    this(null, document);
  }

  public Owns withDocument(DocumentNode document) {
    return new Owns(id, document);
  }

  @Override
  public String toString() {
    return "Owns [document=" + this.document + "]";
  }

}
