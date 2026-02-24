package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.RevisionOf;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.SpecializationOf;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasAttributedTo;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasGeneratedBy;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Entity")
public class EntityNode extends BaseProvClassNode {

  @Relationship(type = "revision_of", direction = Relationship.Direction.OUTGOING)
  private final List<RevisionOf> revisionOf;

  @Relationship(type = "specialization_of", direction = Relationship.Direction.OUTGOING)
  private final List<SpecializationOf> specializationOf;

  @Relationship(type = "was_generated_by", direction = Relationship.Direction.OUTGOING)
  private final List<WasGeneratedBy> wasGeneratedBy;

  @Relationship(type = "was_attributed_to", direction = Relationship.Direction.OUTGOING)
  private final List<WasAttributedTo> wasAttributedTo;

  @PersistenceCreator
  public EntityNode(
      String id,
      String attributesJson,
      List<RevisionOf> revisionOf,
      List<SpecializationOf> specializationOf,
      List<WasGeneratedBy> wasGeneratedBy,
      List<WasAttributedTo> wasAttributedTo) {
    super(id, attributesJson);

    this.revisionOf = revisionOf;
    this.specializationOf = specializationOf;
    this.wasGeneratedBy = wasGeneratedBy;
    this.wasAttributedTo = wasAttributedTo;
  }

  public EntityNode(String id) {
    super(id, "{}");

    this.revisionOf = Collections.emptyList();
    this.specializationOf = Collections.emptyList();
    this.wasGeneratedBy = Collections.emptyList();
    this.wasAttributedTo = Collections.emptyList();
  }

  public @NonNull EntityNode wihtRevisionOfEntity(@Nullable EntityNode entity) {
    if (entity == null)
      return this;

    List<RevisionOf> revisionOf = Stream.concat(
        this.getRevisionOf().stream(),
        Stream.of(new RevisionOf(entity)))
        .collect(Collectors.toList());

    return new EntityNode(
        this.getId(),
        this.getAttributes(),
        revisionOf,
        this.getSpecializationOf(),
        this.getWasGeneratedBy(),
        this.getWasAttributedTo());
  }

  public @NonNull EntityNode wihtRevisionOf(@NonNull List<RevisionOf> revisionOf) {
    return new EntityNode(
        this.getId(),
        this.getAttributes(),
        revisionOf,
        this.specializationOf,
        this.getWasGeneratedBy(),
        this.getWasAttributedTo());
  }

  public @NonNull EntityNode wihtSpecializationOfEntity(@Nullable EntityNode entity) {
    if (entity == null)
      return this;

    List<SpecializationOf> specializationOf = Stream.concat(
        this.getSpecializationOf().stream(),
        Stream.of(new SpecializationOf(entity)))
        .collect(Collectors.toList());

    return new EntityNode(
        this.getId(),
        this.getAttributes(),
        this.getRevisionOf(),
        specializationOf,
        this.getWasGeneratedBy(),
        this.getWasAttributedTo());
  }

  public @NonNull EntityNode wihtSpecializationOf(@NonNull List<SpecializationOf> specializationOf) {
    return new EntityNode(
        this.getId(),
        this.getAttributes(),
        this.getRevisionOf(),
        specializationOf,
        this.getWasGeneratedBy(),
        this.getWasAttributedTo());
  }

  public @NonNull EntityNode wihtWasGeneratedByActivity(@Nullable ActivityNode activity) {
    if (activity == null)
      return this;

    List<WasGeneratedBy> wasGeneratedBy = Stream.concat(
        this.getWasGeneratedBy().stream(),
        Stream.of(new WasGeneratedBy(activity)))
        .collect(Collectors.toList());

    return new EntityNode(
        this.getId(),
        this.getAttributes(),
        this.getRevisionOf(),
        this.getSpecializationOf(),
        wasGeneratedBy,
        this.getWasAttributedTo());
  }

  public @NonNull EntityNode wihtWasGeneratedBy(@NonNull List<WasGeneratedBy> wasGeneratedBy) {
    return new EntityNode(
        this.getId(),
        this.getAttributes(),
        this.getRevisionOf(),
        this.getSpecializationOf(),
        wasGeneratedBy,
        this.getWasAttributedTo());
  }

  // --
  public @NonNull EntityNode wihtWasAttributedToAgent(@Nullable AgentNode agent) {
    if (agent == null)
      return this;

    List<WasAttributedTo> wasAttributedTo = Stream.concat(
        this.getWasAttributedTo().stream(),
        Stream.of(new WasAttributedTo(agent)))
        .collect(Collectors.toList());

    return new EntityNode(
        this.getId(),
        this.getAttributes(),
        this.getRevisionOf(),
        this.getSpecializationOf(),
        this.getWasGeneratedBy(),
        wasAttributedTo);
  }

  public @NonNull EntityNode wihtWasAttributedTo(@NonNull List<WasAttributedTo> wasAttributedTo) {
    return new EntityNode(
        this.getId(),
        this.getAttributes(),
        this.getRevisionOf(),
        this.getSpecializationOf(),
        this.getWasGeneratedBy(),
        wasAttributedTo);
  }

  // ---
  public List<RevisionOf> getRevisionOf() {
    return this.revisionOf;
  }

  public List<SpecializationOf> getSpecializationOf() {
    return this.specializationOf;
  }

  public List<WasGeneratedBy> getWasGeneratedBy() {
    return this.wasGeneratedBy;
  }

  public List<WasAttributedTo> getWasAttributedTo() {
    return this.wasAttributedTo;
  }

}
