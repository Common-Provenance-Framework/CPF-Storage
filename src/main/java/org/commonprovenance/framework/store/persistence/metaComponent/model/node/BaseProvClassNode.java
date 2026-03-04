package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import org.commonprovenance.framework.store.common.dto.HasId;
import org.springframework.data.neo4j.core.schema.Id;

public class BaseProvClassNode implements HasId {
  @Id
  private final String id;

  private final String attributesJson;

  public BaseProvClassNode(String id, String attributesJson) {
    this.id = id;
    this.attributesJson = attributesJson;
  }

  public String getId() {
    return this.id;
  }

  public String getAttributes() {
    return attributesJson;
  }

}
