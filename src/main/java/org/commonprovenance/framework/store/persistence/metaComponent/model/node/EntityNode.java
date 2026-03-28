package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.RevisionOf;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.SpecializationOf;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasAttributedTo;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasDerivedFrom;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasGeneratedBy;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.CompositeProperty;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Entity")
public class EntityNode extends BaseProvClassNode {
  @CompositeProperty(prefix = "pav", delimiter = ":")
  private final Map<String, Object> pav;

  @Relationship(type = "revision_of", direction = Relationship.Direction.OUTGOING)
  private final List<RevisionOf> revisionOf;

  @Relationship(type = "specialization_of", direction = Relationship.Direction.OUTGOING)
  private final List<SpecializationOf> specializationOf;

  @Relationship(type = "was_generated_by", direction = Relationship.Direction.OUTGOING)
  private final List<WasGeneratedBy> wasGeneratedBy;

  @Relationship(type = "was_attributed_to", direction = Relationship.Direction.OUTGOING)
  private final List<WasAttributedTo> wasAttributedTo;

  @Relationship(type = "was_derived_from", direction = Relationship.Direction.OUTGOING)
  private final List<WasDerivedFrom> wasDerivedFrom;

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public EntityNode(
      String id,
      String identifier,
      String provType,
      Map<String, Object> cpm,
      Map<String, Object> pav,
      List<RevisionOf> revisionOf,
      List<SpecializationOf> specializationOf,
      List<WasGeneratedBy> wasGeneratedBy,
      List<WasAttributedTo> wasAttributedTo,
      List<WasDerivedFrom> wasDerivedFrom) {
    super(id, identifier, provType, Optional.ofNullable(cpm).map(Map::copyOf).orElse(Map.of()));

    this.pav = Optional.ofNullable(pav).map(Map::copyOf).orElse(Map.of());

    this.revisionOf = revisionOf;
    this.specializationOf = specializationOf;
    this.wasGeneratedBy = wasGeneratedBy;
    this.wasAttributedTo = wasAttributedTo;
    this.wasDerivedFrom = wasDerivedFrom;
  }

  // Constructor for creating new node (id will be generated)
  public EntityNode(
      String identifier,
      String provType,
      Map<String, Object> cpm,
      Map<String, Object> pav) {
    super(identifier, provType, Optional.ofNullable(cpm).map(Map::copyOf).orElse(Map.of()));

    this.pav = Optional.ofNullable(pav).map(Map::copyOf).orElse(Map.of());

    this.revisionOf = Collections.emptyList();
    this.specializationOf = Collections.emptyList();
    this.wasGeneratedBy = Collections.emptyList();
    this.wasAttributedTo = Collections.emptyList();
    this.wasDerivedFrom = Collections.emptyList();
  }

  public EntityNode(
      String identifier,
      String provType,
      Map<String, Object> cpm) {
    super(identifier, provType, Optional.ofNullable(cpm).map(Map::copyOf).orElse(Map.of()));

    this.pav = Map.of();

    this.revisionOf = Collections.emptyList();
    this.specializationOf = Collections.emptyList();
    this.wasGeneratedBy = Collections.emptyList();
    this.wasAttributedTo = Collections.emptyList();
    this.wasDerivedFrom = Collections.emptyList();
  }

  public EntityNode(
      String identifier,
      String provType) {
    super(identifier, provType, Map.of());

    this.pav = Map.of();

    this.revisionOf = Collections.emptyList();
    this.specializationOf = Collections.emptyList();
    this.wasGeneratedBy = Collections.emptyList();
    this.wasAttributedTo = Collections.emptyList();
    this.wasDerivedFrom = Collections.emptyList();
  }

  // Factory methods
  public EntityNode withRevisionOf(List<RevisionOf> revisionOf) {
    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        this.getPav(),
        revisionOf,
        this.specializationOf,
        this.getWasGeneratedBy(),
        this.getWasAttributedTo(),
        this.getWasDerivedFrom());
  }

  public EntityNode withRevisionOfEntity(EntityNode entity) {
    if (entity == null)
      return this;

    List<RevisionOf> revisionOf = Stream.concat(
        this.getRevisionOf().stream(),
        Stream.of(new RevisionOf(entity)))
        .collect(Collectors.toList());

    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        this.getPav(),
        revisionOf,
        this.getSpecializationOf(),
        this.getWasGeneratedBy(),
        this.getWasAttributedTo(),
        this.getWasDerivedFrom());
  }

  public EntityNode withSpecializationOf(List<SpecializationOf> specializationOf) {
    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        this.getPav(),
        this.getRevisionOf(),
        specializationOf,
        this.getWasGeneratedBy(),
        this.getWasAttributedTo(),
        this.getWasDerivedFrom());
  }

  public EntityNode withSpecializationOfEntity(EntityNode entity) {
    if (entity == null)
      return this;

    List<SpecializationOf> specializationOf = Stream.concat(
        this.getSpecializationOf().stream(),
        Stream.of(new SpecializationOf(entity)))
        .collect(Collectors.toList());

    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        this.getPav(),
        this.getRevisionOf(),
        specializationOf,
        this.getWasGeneratedBy(),
        this.getWasAttributedTo(),
        this.getWasDerivedFrom());
  }

  public EntityNode withWasGeneratedBy(List<WasGeneratedBy> wasGeneratedBy) {
    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        this.getPav(),
        this.getRevisionOf(),
        this.getSpecializationOf(),
        wasGeneratedBy,
        this.getWasAttributedTo(),
        this.getWasDerivedFrom());
  }

  public EntityNode withWasGeneratedByActivity(ActivityNode activity) {
    if (activity == null)
      return this;

    List<WasGeneratedBy> wasGeneratedBy = Stream.concat(
        this.getWasGeneratedBy().stream(),
        Stream.of(new WasGeneratedBy(activity)))
        .collect(Collectors.toList());

    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        this.getPav(),
        this.getRevisionOf(),
        this.getSpecializationOf(),
        wasGeneratedBy,
        this.getWasAttributedTo(),
        this.getWasDerivedFrom());
  }

  public EntityNode withWasAttributedTo(List<WasAttributedTo> wasAttributedTo) {
    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        this.getPav(),
        this.getRevisionOf(),
        this.getSpecializationOf(),
        this.getWasGeneratedBy(),
        wasAttributedTo,
        this.getWasDerivedFrom());
  }

  public EntityNode withWasAttributedToAgent(AgentNode agent) {
    if (agent == null)
      return this;

    List<WasAttributedTo> wasAttributedTo = Stream.concat(
        this.getWasAttributedTo().stream(),
        Stream.of(new WasAttributedTo(agent)))
        .collect(Collectors.toList());

    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        this.getPav(),
        this.getRevisionOf(),
        this.getSpecializationOf(),
        this.getWasGeneratedBy(),
        wasAttributedTo,
        this.getWasDerivedFrom());
  }

  public EntityNode withWasDerivedFrom(List<WasDerivedFrom> wasDerivedFrom) {
    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        this.getPav(),
        this.getRevisionOf(),
        this.getSpecializationOf(),
        this.getWasGeneratedBy(),
        this.getWasAttributedTo(),
        wasDerivedFrom);
  }

  public EntityNode withWasDerivedFromEntity(EntityNode entity) {
    if (entity == null)
      return this;

    List<WasDerivedFrom> wasDerivedFrom = Stream.concat(
        this.getWasDerivedFrom().stream(),
        Stream.of(new WasDerivedFrom(entity)))
        .collect(Collectors.toList());

    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        this.getPav(),
        this.getRevisionOf(),
        this.getSpecializationOf(),
        this.getWasGeneratedBy(),
        this.getWasAttributedTo(),
        wasDerivedFrom);
  }

  public EntityNode withVersion(Integer version) {
    Map<String, Object> pav = new HashMap<>(this.getPav());
    pav.put("version", version);
    return new EntityNode(
        this.getId(),
        this.getIdentifier(),
        this.getProvType(),
        this.getCpm(),
        pav,
        this.getRevisionOf(),
        this.getSpecializationOf(),
        this.getWasGeneratedBy(),
        this.getWasAttributedTo(),
        this.getWasDerivedFrom());
  }

  public Map<String, Object> getPav() {
    return this.pav;
  }

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

  public List<WasDerivedFrom> getWasDerivedFrom() {
    return this.wasDerivedFrom;
  }

}
