package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import java.util.function.Function;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import reactor.core.publisher.Mono;

public interface BundleRepository {
  Mono<Void> create(String identifier);

  Mono<EntityNode> addToken(String metaBundleIdentifier, String jwtToken);

  Mono<Boolean> hasVersionEntity(String identifier);

  Mono<Boolean> existsByIdentifier(String identifier);

  Mono<Boolean> notExistsByIdentifier(String identifier);

  Mono<BundleNode> findByIdentifier(String identifier);

  Function<EntityNode, Mono<Void>> addTokenToMetaBundle(String identifier);

  Function<EntityNode, Mono<Void>> addTokenGenerationToBundle(String identifier);

  Function<EntityNode, Mono<Void>> addTokenGeneratorToBundle(String identifier);
}
