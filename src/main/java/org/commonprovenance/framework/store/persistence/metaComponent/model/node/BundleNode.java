package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.BundleActivities;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.BundleAgents;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.BundleEntities;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import jakarta.validation.constraints.NotNull;

@Node("Bundle")
public class BundleNode extends BaseProvClassNode {

  @Relationship(type = "bundle_entities", direction = Relationship.Direction.OUTGOING)
  private final List<BundleEntities> bundleEntities;

  @Relationship(type = "bundle_activities", direction = Relationship.Direction.OUTGOING)
  private final List<BundleActivities> bundleActivities;

  @Relationship(type = "bundle_agents", direction = Relationship.Direction.OUTGOING)
  private final List<BundleAgents> bundleAgents;

  @PersistenceCreator
  public BundleNode(
      String id,
      String attributesJson,
      List<BundleEntities> bundleEntities,
      List<BundleActivities> bundleActivities,
      List<BundleAgents> bundleAgents) {
    super(id, attributesJson);

    this.bundleEntities = bundleEntities;
    this.bundleActivities = bundleActivities;
    this.bundleAgents = bundleAgents;
  }

  public BundleNode(String id,
      Collection<EntityNode> entities,
      Collection<AgentNode> agents,
      Collection<ActivityNode> activities) {
    super(id, "{}");
    this.bundleEntities = entities.stream().map(e -> new BundleEntities(e)).collect(Collectors.toList());
    this.bundleActivities = activities.stream().map(e -> new BundleActivities(e)).collect(Collectors.toList());
    this.bundleAgents = agents.stream().map(e -> new BundleAgents(e)).collect(Collectors.toList());
  }

  public BundleNode(String id) {
    super(id, "{}");

    this.bundleEntities = Collections.emptyList();
    this.bundleActivities = Collections.emptyList();
    this.bundleAgents = Collections.emptyList();
  }

  public @NonNull BundleNode withEntity(@Nullable EntityNode entity) {
    if (entity == null)
      return this;

    List<BundleEntities> bundleEntities = Stream.concat(
        this.getBundleEntities().stream(),
        Stream.of(new BundleEntities(entity)))
        .collect(Collectors.toList());

    return new BundleNode(
        this.getId(),
        this.getAttributes(),
        bundleEntities,
        this.getBundleActivities(),
        this.getBundleAgents());
  }

  public @NonNull BundleNode withBundleEntities(@NonNull List<BundleEntities> bundleEntities) {
    return new BundleNode(
        this.getId(),
        this.getAttributes(),
        bundleEntities,
        this.getBundleActivities(),
        this.getBundleAgents());
  }

  public @NonNull BundleNode withActivity(@Nullable ActivityNode activity) {
    if (activity == null)
      return this;

    List<BundleActivities> bundleActivities = Stream.concat(
        this.getBundleActivities().stream(),
        Stream.of(new BundleActivities(activity)))
        .collect(Collectors.toList());

    return new BundleNode(
        this.getId(),
        this.getAttributes(),
        this.getBundleEntities(),
        bundleActivities,
        this.getBundleAgents());
  }

  public @NonNull BundleNode withBundleActivities(@NonNull List<BundleActivities> bundleActivities) {
    return new BundleNode(
        this.getId(),
        this.getAttributes(),
        this.getBundleEntities(),
        bundleActivities,
        this.getBundleAgents());
  }

  public @NonNull BundleNode withAgent(@Nullable AgentNode agent) {
    if (agent == null)
      return this;

    List<BundleAgents> bundleAgents = Stream.concat(
        this.getBundleAgents().stream(),
        Stream.of(new BundleAgents(agent)))
        .collect(Collectors.toList());

    return new BundleNode(
        this.getId(),
        this.getAttributes(),
        this.getBundleEntities(),
        this.getBundleActivities(),
        bundleAgents);
  }

  public @NonNull BundleNode withBundleAgents(@NonNull List<BundleAgents> bundleAgents) {
    return new BundleNode(
        this.getId(),
        this.getAttributes(),
        this.getBundleEntities(),
        this.getBundleActivities(),
        bundleAgents);
  }

  public @NotNull BundleNode withBundleNodes(
      @NotNull List<BundleEntities> bundleEntities,
      @NotNull List<BundleActivities> bundleActivities,
      @NotNull List<BundleAgents> bundleAgents) {
    return new BundleNode(
        this.getId(),
        this.getAttributes(),
        bundleEntities,
        bundleActivities,
        bundleAgents);
  }

  public @NotNull BundleNode withBundleNodes(@NotNull List<BaseProvClassNode> nodes) {
    return new BundleNode(
        this.getId(),
        this.getAttributes(),
        nodes.stream()
            .filter(BundleEntities.class::isInstance)
            .map(BundleEntities.class::cast)
            .collect(Collectors.toList()),
        nodes.stream()
            .filter(BundleActivities.class::isInstance)
            .map(BundleActivities.class::cast)
            .collect(Collectors.toList()),
        nodes.stream()
            .filter(BundleAgents.class::isInstance)
            .map(BundleAgents.class::cast)
            .collect(Collectors.toList()));
  }

  public List<BundleEntities> getBundleEntities() {
    return bundleEntities;
  }

  public List<BundleActivities> getBundleActivities() {
    return bundleActivities;
  }

  public List<BundleAgents> getBundleAgents() {
    return bundleAgents;
  }

  public List<BaseProvClassNode> getAllNodes() {
    return Stream.concat(
        Stream.concat(
            this.getBundleEntities().stream().map(BundleEntities::getEntity),
            this.getBundleActivities().stream().map(BundleActivities::getActivity)),
        this.getBundleAgents().stream().map(BundleAgents::getAgent))
        .collect(Collectors.toList());
  }
}
