package org.commonprovenance.framework.store.persistence.metaComponent.model.node;

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

  public BundleNode(String id) {
    super(id, "{}");

    this.contains = Collections.emptyList();
  }

  public @NonNull BundleNode wihtNode(@Nullable BaseProvClassNode node) {
    if (node == null)
      return this;

    List<Contains> contains = Stream.concat(
        this.getContains().stream(),
        Stream.of(new Contains(node)))
        .collect(Collectors.toList());

    return new BundleNode(this.getId(), this.getAttributes(), contains);
  }

  public @NonNull BundleNode wihtContains(@NonNull List<Contains> contains) {
    return new BundleNode(this.getId(), this.getAttributes(), contains);
  }

  public List<Contains> getContains() {
    return contains;
  }

}
