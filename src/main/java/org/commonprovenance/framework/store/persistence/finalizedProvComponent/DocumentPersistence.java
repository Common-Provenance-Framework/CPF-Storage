package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Document;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentPersistence {
  @NotNull
  Mono<Document> create(@NotNull Document document);

  @NotNull
  Flux<Document> getAll();

  @NotNull
  Mono<Document> getById(@NotNull String id);

  @NotNull
  Mono<Void> deleteById(@NotNull String id);
}
