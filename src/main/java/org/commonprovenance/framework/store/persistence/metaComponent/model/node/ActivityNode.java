package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.Used;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasAssociatedWith;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Activity")
public class ActivityNode extends BaseProvClassNode {
  @Property("start_time")
  private final String startTime;

  @Property("end_time")
  private final String endTime;

  @Relationship(type = "was_associated_with", direction = Relationship.Direction.OUTGOING)
  private final List<WasAssociatedWith> wasAssociatedWith;

  @Relationship(type = "used", direction = Relationship.Direction.OUTGOING)
  private final List<Used> used;

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public ActivityNode(
      String id,
      String identifier,
      String provType,
      String startTime,
      String endTime,
      Map<String, Object> cpm,
      List<WasAssociatedWith> wasAssociatedWith,
      List<Used> used) {
    super(id, identifier, provType, Optional.ofNullable(cpm).map(Map::copyOf).orElse(Map.of()));

    this.startTime = startTime;
    this.endTime = endTime;

    this.wasAssociatedWith = wasAssociatedWith;
    this.used = used;
  }

  // Constructor for creating new node (id will be generated)
  public ActivityNode(
      String identifier,
      String provType,
      String startTime,
      String endTime,
      Map<String, Object> cpm) {
    super(identifier, provType, Optional.ofNullable(cpm).map(Map::copyOf).orElse(Map.of()));

    this.startTime = startTime;
    this.endTime = endTime;
    this.wasAssociatedWith = Collections.emptyList();
    this.used = Collections.emptyList();
  }

  public ActivityNode(
      String identifier,
      String provType,
      String startTime,
      String endTime) {
    super(identifier, provType, Map.of());

    this.startTime = startTime;
    this.endTime = endTime;
    this.wasAssociatedWith = Collections.emptyList();
    this.used = Collections.emptyList();
  }

  // Factory methods
  public ActivityNode withId(String id) {
    return new ActivityNode(
        id,
        this.getIdentifier(),
        this.getProvType(),
        this.getStartTime(),
        this.getEndTime(),
        this.getCpm(),
        this.getWasAssociatedWith(),
        this.getUsed());
  }

  public ActivityNode withWasAssociatedWith(List<WasAssociatedWith> wasAssociatedWith) {
    return new ActivityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getStartTime(),
        this.getEndTime(),
        this.getCpm(),
        wasAssociatedWith,
        this.getUsed());
  }

  public ActivityNode withWasAssociatedWithAgent(AgentNode agent) {
    if (agent == null)
      return this;

    List<WasAssociatedWith> wasAssociatedWith = Stream.concat(
        this.getWasAssociatedWith().stream(),
        Stream.of(new WasAssociatedWith(agent)))
        .collect(Collectors.toList());

    return new ActivityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getStartTime(),
        this.getEndTime(),
        this.getCpm(),
        wasAssociatedWith,
        this.used);
  }

  public ActivityNode withUsed(List<Used> used) {
    return new ActivityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getStartTime(),
        this.getEndTime(),
        this.getCpm(),
        this.getWasAssociatedWith(),
        used);
  }

  public ActivityNode withUsedEntity(EntityNode entity) {
    if (entity == null)
      return this;

    List<Used> used = Stream.concat(
        this.getUsed().stream(),
        Stream.of(new Used(entity)))
        .collect(Collectors.toList());

    return new ActivityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getStartTime(),
        this.getEndTime(),
        this.getCpm(),
        this.getWasAssociatedWith(),
        used);
  }

  // Getters
  public String getStartTime() {
    return startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public List<WasAssociatedWith> getWasAssociatedWith() {
    return wasAssociatedWith;
  }

  public List<Used> getUsed() {
    return used;
  }

  public AgentNode getTokenGenerator() throws ConflictException {
    if (this.wasAssociatedWith.size() == 0)
      throw new ConflictException("Token generation is not associated with token generator!");

    if (this.wasAssociatedWith.size() > 1)
      throw new ConflictException("Token generation is associated with more than one token generator!");

    return this.wasAssociatedWith.getFirst().getAgent();
  }
}
