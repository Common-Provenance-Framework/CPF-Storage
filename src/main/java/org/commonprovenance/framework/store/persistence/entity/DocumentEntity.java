package org.commonprovenance.framework.store.persistence.entity;

import org.commonprovenance.framework.store.common.dto.HasFormat;
import org.commonprovenance.framework.store.common.dto.HasId;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Document")
public class DocumentEntity implements HasId, HasFormat {
  @Id
  private final String id;
  private final String graph;
  private final String format;
  private final String signature;

  public DocumentEntity(String id, String graph, String format, String signature) {
    this.id = id;
    this.graph = graph;
    this.format = format;
    this.signature = signature;
  }

  public String getId() {
    return this.id;
  }

  public String getGraph() {
    return this.graph;
  }

  public String getFormat() {
    return this.format;
  }

  public String getSignature() {
    return signature;
  }

}
