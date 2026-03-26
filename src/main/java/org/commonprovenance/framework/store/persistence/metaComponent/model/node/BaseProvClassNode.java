package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import java.util.Map;

import org.commonprovenance.framework.store.common.dto.HasId;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.CompositeProperty;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;

public class BaseProvClassNode implements HasId {
  @Id
  @GeneratedValue
  private final String id;

  private final String identifier;

  @Property("prov:type")
  private final String provType;

  @CompositeProperty(prefix = "cpm", delimiter = ":")
  private final Map<String, Object> cpm;

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public BaseProvClassNode(
      String id,
      String identifier,
      String provType,
      Map<String, Object> cpm) {
    this.id = id;
    this.identifier = identifier;
    this.provType = provType;
    this.cpm = cpm;
  }

  // Constructor for creating new node (id will be generated)
  public BaseProvClassNode(
      String identifier,
      String provType,
      Map<String, Object> cpm) {
    this.id = null;
    this.identifier = identifier;
    this.provType = provType;
    this.cpm = cpm;
  }

  public String getId() {
    return this.id;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getProvType() {
    return provType;
  }

  public Map<String, Object> getCpm() {
    return cpm;
  }

}
