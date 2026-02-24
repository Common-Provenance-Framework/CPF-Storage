package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.Contains;
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node("Bundle")
public class BundleNode extends BaseProvClassNode {

  @Relationship(type = "contains", direction = Relationship.Direction.OUTGOING)
  private final List<Contains> contains;

  @PersistenceCreator
  public BundleNode(String id, String attributesJson, List<Contains> contains) {
    super(id, attributesJson);

    this.contains = contains;
  }

  public BundleNode(String id,
      Collection<EntityNode> entities,
      Collection<AgentNode> agents,
      Collection<ActivityNode> activities) {
    super(id, "{}");

    this.contains = Stream.concat(
        Stream.concat(
            entities.stream(),
            agents.stream()),
        activities.stream())
        .map(s -> new Contains(s))
        .collect(Collectors.toList());
  }

  public BundleNode(String id) {
    super(id, "{}");

    this.contains = Collections.emptyList();
  }

  public @NonNull BundleNode withNode(@Nullable BaseProvClassNode node) {
    if (node == null)
      return this;

    List<Contains> contains = Stream.concat(
        this.getContains().stream(),
        Stream.of(new Contains(node)))
        .collect(Collectors.toList());

    return new BundleNode(this.getId(), this.getAttributes(), contains);
  }

  public @NonNull BundleNode withContains(@NonNull List<Contains> contains) {
    return new BundleNode(this.getId(), this.getAttributes(), contains);
  }

  public List<Contains> getContains() {
    return contains;
  }

}
