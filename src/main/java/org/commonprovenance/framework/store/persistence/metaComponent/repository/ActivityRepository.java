package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;

import reactor.core.publisher.Mono;

public interface ActivityRepository {
  Mono<ActivityNode> save(ActivityNode activity);

  Mono<ActivityNode> findByIdentifier(String identifier);
}
