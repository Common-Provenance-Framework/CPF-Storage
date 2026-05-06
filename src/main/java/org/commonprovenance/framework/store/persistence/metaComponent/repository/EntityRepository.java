package org.commonprovenance.framework.store.persistence.metaComponent.repository;

import java.util.function.Function;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import reactor.core.publisher.Mono;

public interface EntityRepository {
  Mono<EntityNode> addFirstVersion(String metaBundleIdentifier, String versionIdentifier);

  Mono<EntityNode> addVersion(String metaBundleIdentifier, String versionIdentifier);

  Mono<EntityNode> addToken(String metaBundleIdentifier, String jwtToken);

  Function<EntityNode, Mono<Void>> makeSpecializationOfGeneralVersion(String metaBundleIdentifier);

  Function<EntityNode, Mono<Void>> makeRevisionOfVersion(String metaBundleIdentifier, Integer version);

}
