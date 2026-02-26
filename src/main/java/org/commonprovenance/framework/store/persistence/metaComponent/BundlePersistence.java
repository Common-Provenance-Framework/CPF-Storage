package org.commonprovenance.framework.store.persistence.metaComponent;

import org.openprovenance.prov.model.Document;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Mono;

public interface BundlePersistence {
  @NotNull
  Mono<Document> create(@NotNull Document bundle);

  @NotNull
  Mono<Document> getById(@NotNull String id);

  @NotNull
  Mono<Boolean> exists(@NotNull String id);

}
