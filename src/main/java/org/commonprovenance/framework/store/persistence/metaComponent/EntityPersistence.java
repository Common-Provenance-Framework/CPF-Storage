package org.commonprovenance.framework.store.persistence.metaComponent;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Mono;

public interface EntityPersistence {
  @NotNull
  Mono<EntityNode> create(@NotNull EntityNode entity);

  @NotNull
  Mono<EntityNode> getById(@NotNull String id);

}
