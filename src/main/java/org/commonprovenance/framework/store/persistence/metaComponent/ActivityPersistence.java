package org.commonprovenance.framework.store.persistence.metaComponent;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Mono;

public interface ActivityPersistence {
  @NotNull
  Mono<ActivityNode> create(@NotNull ActivityNode activity);

  @NotNull
  Mono<ActivityNode> getById(@NotNull String id);

}
