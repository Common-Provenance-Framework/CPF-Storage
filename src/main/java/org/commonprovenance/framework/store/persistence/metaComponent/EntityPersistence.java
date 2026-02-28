package org.commonprovenance.framework.store.persistence.metaComponent;

import java.util.function.Function;

import org.openprovenance.prov.model.Entity;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Mono;

public interface EntityPersistence {
  @NotNull
  Mono<Entity> create(@NotNull Entity entity);

  @NotNull
  Function<Entity, Mono<Entity>> addFirstVersion(@NotNull Entity general);

  @NotNull
  Mono<Entity> getById(@NotNull String id);

}
