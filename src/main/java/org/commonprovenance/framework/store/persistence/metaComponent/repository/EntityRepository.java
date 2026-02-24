package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import reactor.core.publisher.Mono;

public interface EntityRepository {
  Mono<EntityNode> save(EntityNode entity);

  Mono<EntityNode> findById(String id);
}
