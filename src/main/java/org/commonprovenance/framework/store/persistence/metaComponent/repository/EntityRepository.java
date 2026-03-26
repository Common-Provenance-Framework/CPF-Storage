package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EntityRepository {
  Mono<EntityNode> save(EntityNode entity);

  Mono<EntityNode> findByIdentifier(String identifier);

  Flux<EntityNode> getAllEntitiesByBundleIdentifier(String bundleIdentifier);

  Mono<EntityNode> getGeneralEntityByBundleIdentifier(String bundleIdentifier);

  Mono<EntityNode> getLastVersionEntityByBundleIdentifier(String bundleIdentifier);

  Mono<Integer> getLastVersionByBundleIdentifier(String bundleIdentifier);
}
