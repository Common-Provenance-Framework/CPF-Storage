package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import java.util.function.Function;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;

import reactor.core.publisher.Mono;

public interface BundleRepository {
  Mono<Void> create(String identifier);

  Function<String, Mono<Void>> addToken(String identifier);

  Mono<Boolean> hasVersionEntity(String identifier);

  Mono<Boolean> existsByIdentifier(String identifier);

  Mono<Boolean> notExistsByIdentifier(String identifier);

  Mono<BundleNode> findByIdentifier(String identifier);

}
