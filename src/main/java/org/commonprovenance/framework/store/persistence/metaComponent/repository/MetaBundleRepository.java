package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import java.util.function.Function;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import reactor.core.publisher.Mono;

public interface MetaBundleRepository {
  Mono<Void> create(String metaBundleIdentifier);

  Mono<Boolean> hasVersionEntity(String metaBundleIdentifier);

  Mono<Boolean> existsByIdentifier(String metaBundleIdentifier);

  Mono<Boolean> notExistsByIdentifier(String metaBundleIdentifier);

  Mono<BundleNode> findByIdentifier(String metaBundleIdentifier);

  Function<EntityNode, Mono<Void>> addEntityToMetaBundle(String metaBundleIdentifier);

  Function<EntityNode, Mono<Void>> addTokenGenerationToMetaBundle(String metaBundleIdentifier);

  Function<EntityNode, Mono<Void>> addTokenGeneratorToMetaBundle(String metaBundleIdentifier);
}
