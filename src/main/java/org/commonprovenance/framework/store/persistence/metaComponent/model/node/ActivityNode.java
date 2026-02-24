package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.Used;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasAssociatedWith;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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

  @PersistenceCreator
  public ActivityNode(
      String id,
      String startTime,
      String endTime,
      String attributesJson,
      List<WasAssociatedWith> wasAssociatedWith,
      List<Used> used) {
    super(id, attributesJson);

    this.startTime = startTime;
    this.endTime = endTime;

    this.wasAssociatedWith = wasAssociatedWith;
    this.used = used;
  }

  public ActivityNode(String id, String startTime, String endTime, String attributes) {
    super(id, attributes);

    this.startTime = startTime;
    this.endTime = endTime;
    this.wasAssociatedWith = Collections.emptyList();
    this.used = Collections.emptyList();
  }

  public @NonNull ActivityNode withWasAssociatedWithAgent(@Nullable AgentNode agent) {
    if (agent == null)
      return this;

    List<WasAssociatedWith> wasAssociatedWith = Stream.concat(
        this.getWasAssociatedWith().stream(),
        Stream.of(new WasAssociatedWith(agent)))
        .collect(Collectors.toList());

    return new ActivityNode(
        this.getId(),
        this.getStartTime(),
        this.getEndTime(),
        this.getAttributes(),
        wasAssociatedWith,
        this.used);
  }

  public @NonNull ActivityNode withWasAssociatedWith(@NonNull List<WasAssociatedWith> wasAssociatedWith) {
    return new ActivityNode(
        this.getId(),
        this.getStartTime(),
        this.getEndTime(),
        this.getAttributes(),
        wasAssociatedWith,
        this.used);
  }

  public @NonNull ActivityNode withUsedEntity(@Nullable EntityNode entity) {
    if (entity == null)
      return this;

    List<Used> used = Stream.concat(
        this.getUsed().stream(),
        Stream.of(new Used(entity)))
        .collect(Collectors.toList());

    return new ActivityNode(
        this.getId(),
        this.getStartTime(),
        this.getEndTime(),
        this.getAttributes(),
        this.getWasAssociatedWith(),
        used);
  }

  public @NonNull ActivityNode withUsed(@NonNull List<Used> used) {
    return new ActivityNode(
        this.getId(),
        this.getStartTime(),
        this.getEndTime(),
        this.getAttributes(),
        this.getWasAssociatedWith(),
        used);
  }

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

}
