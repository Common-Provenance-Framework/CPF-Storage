package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import java.util.Map;

import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Agent")
public class AgentNode extends BaseProvClassNode {

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public AgentNode(
      String id,
      String identifier,
      String provType,
      Map<String, Object> cpm) {
    super(id, identifier, provType, cpm);
  }

  // Constructor for creating new node (id will be generated)
  public AgentNode(
      String identifier,
      String provType,
      Map<String, Object> cpm) {
    super(identifier, provType, cpm);
  }

  // Factory methods
  public AgentNode withId(String id) {
    return new AgentNode(
        id,
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm());
  }

}
