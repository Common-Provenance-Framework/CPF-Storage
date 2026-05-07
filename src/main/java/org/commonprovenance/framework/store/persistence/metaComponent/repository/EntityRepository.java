package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import java.util.function.Function;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import reactor.core.publisher.Mono;

public interface EntityRepository {
  Function<Integer, Mono<EntityNode>> createBundleVersionEntity(String metaBundleIdentifier, String versionIdentifier);

  Mono<EntityNode> createBundleTokenEntity(String metaBundleIdentifier, String jwtToken);

  Function<EntityNode, Mono<Void>> addToBundleVersionEntity(String jwtToken);

  Function<EntityNode, Mono<Void>> makeSpecializationOfGeneralVersion(String metaBundleIdentifier);

  Function<EntityNode, Mono<Void>> makeRevisionOfPreviousVersion(String metaBundleIdentifier);

}
