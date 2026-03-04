package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Agent")
public class AgentNode extends BaseProvClassNode {

  @PersistenceCreator
  public AgentNode(String id, String attributesJson) {
    super(id, attributesJson);
  }

  public AgentNode(String id) {
    super(id, "{}");
  }

}
