package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.BundleActivities;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.BundleAgents;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.BundleEntities;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Bundle")
public class BundleNode {
  @Id
  @GeneratedValue
  private final String id;

  private final String identifier;

  @Relationship(type = "bundle_entities", direction = Relationship.Direction.OUTGOING)
  private final List<BundleEntities> bundleEntities;

  @Relationship(type = "bundle_activities", direction = Relationship.Direction.OUTGOING)
  private final List<BundleActivities> bundleActivities;

  @Relationship(type = "bundle_agents", direction = Relationship.Direction.OUTGOING)
  private final List<BundleAgents> bundleAgents;

  // Constructor for full initialization (used by Neo4j when reading)
  @PersistenceCreator
  public BundleNode(
      String id,
      String identifier,
      List<BundleEntities> bundleEntities,
      List<BundleActivities> bundleActivities,
      List<BundleAgents> bundleAgents) {

    this.id = id;
    this.identifier = identifier;

    this.bundleEntities = bundleEntities;
    this.bundleActivities = bundleActivities;
    this.bundleAgents = bundleAgents;
  }

  // Constructor for creating new node (id will be generated)
  public BundleNode(String identifier) {
    this.id = null;
    this.identifier = identifier;
    this.bundleEntities = Collections.emptyList();
    this.bundleActivities = Collections.emptyList();
    this.bundleAgents = Collections.emptyList();
  }

  public BundleNode withBundleEntities(List<BundleEntities> bundleEntities) {
    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
        bundleEntities,
        this.getBundleActivities(),
        this.getBundleAgents());
  }

  public BundleNode withEntity(EntityNode entity) {
    if (entity == null)
      return this;

    List<BundleEntities> bundleEntities = Stream.concat(
        this.getBundleEntities().stream(),
        Stream.of(new BundleEntities(entity)))
        .collect(Collectors.toList());

    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
        bundleEntities,
        this.getBundleActivities(),
        this.getBundleAgents());
  }

  public BundleNode withEntities(Collection<EntityNode> entities) {
    if (entities == null || entities.isEmpty())
      return this;

    List<BundleEntities> bundleEntities = Stream.concat(
        this.getBundleEntities().stream(),
        entities.stream().map(BundleEntities::new))
        .collect(Collectors.toList());

    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
        bundleEntities,
        this.getBundleActivities(),
        this.getBundleAgents());
  }

  public BundleNode withBundleActivities(List<BundleActivities> bundleActivities) {
    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
        this.getBundleEntities(),
        bundleActivities,
        this.getBundleAgents());
  }

  public BundleNode withActivity(ActivityNode activity) {
    if (activity == null)
      return this;

    List<BundleActivities> bundleActivities = Stream.concat(
        this.getBundleActivities().stream(),
        Stream.of(new BundleActivities(activity)))
        .collect(Collectors.toList());

    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
        this.getBundleEntities(),
        bundleActivities,
        this.getBundleAgents());
  }

  public BundleNode withActivities(Collection<ActivityNode> activities) {
    if (activities == null || activities.isEmpty())
      return this;

    List<BundleActivities> bundleActivities = Stream.concat(
        this.getBundleActivities().stream(),
        activities.stream().map(BundleActivities::new))
        .collect(Collectors.toList());

    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
        this.getBundleEntities(),
        bundleActivities,
        this.getBundleAgents());
  }

  public BundleNode withBundleAgents(List<BundleAgents> bundleAgents) {
    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
        this.getBundleEntities(),
        this.getBundleActivities(),
        bundleAgents);
  }

  public BundleNode withAgent(AgentNode agent) {
    if (agent == null)
      return this;

    List<BundleAgents> bundleAgents = Stream.concat(
        this.getBundleAgents().stream(),
        Stream.of(new BundleAgents(agent)))
        .collect(Collectors.toList());

    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
        this.getBundleEntities(),
        this.getBundleActivities(),
        bundleAgents);
  }

  public BundleNode withAgents(Collection<AgentNode> agents) {
    if (agents == null || agents.isEmpty())
      return this;

    List<BundleAgents> bundleAgents = Stream.concat(
        this.getBundleAgents().stream(),
        agents.stream().map(BundleAgents::new))
        .collect(Collectors.toList());

    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
        this.getBundleEntities(),
        this.getBundleActivities(),
        bundleAgents);
  }

  public BundleNode withBundleNodes(
      List<BundleEntities> bundleEntities,
      List<BundleActivities> bundleActivities,
      List<BundleAgents> bundleAgents) {
    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
        bundleEntities,
        bundleActivities,
        bundleAgents);
  }

  public BundleNode withBundleNodes(List<BaseProvClassNode> nodes) {
    return new BundleNode(
        this.getId(),
        this.getIdentifier(),
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

  public String getId() {
    return id;
  }

  public String getIdentifier() {
    return identifier;
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
