package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import reactor.core.publisher.Mono;

public interface BundleRepository {
  Mono<BundleNode> save(BundleNode bundle);

  Mono<BundleNode> findByIdentifier(String identifier);

  Mono<BundleNode> findByGeneralEntity(EntityNode entity);

  Mono<Boolean> exists(String identifier);
}
