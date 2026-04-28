package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import java.util.function.Function;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import reactor.core.publisher.Mono;

public interface BundleRepository {
  Mono<BundleNode> create(String identifier);

  Function<String, Mono<Void>> addVersionEntity(String identifier);

  Mono<Boolean> existsByIdentifier(String identifier);

  Mono<Boolean> notExistsByIdentifier(String identifier);

  Mono<BundleNode> save(BundleNode bundle);
  // --- old API

  Mono<BundleNode> findByIdentifier(String identifier);

  Mono<BundleNode> findByGeneralEntity(EntityNode entity);

  Mono<Boolean> hasVersionEntity(String identifier);

  Mono<Boolean> hasNotVersionEntity(String identifier);

}
