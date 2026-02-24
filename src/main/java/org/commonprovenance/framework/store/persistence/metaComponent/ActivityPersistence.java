package org.commonprovenance.framework.store.persistence.metaComponent;

import java.util.UUID;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Mono;

public interface ActivityPersistence {
  @NotNull
  Mono<ActivityNode> create(@NotNull ActivityNode activity);

  @NotNull
  Mono<ActivityNode> getById(@NotNull UUID id);

}
