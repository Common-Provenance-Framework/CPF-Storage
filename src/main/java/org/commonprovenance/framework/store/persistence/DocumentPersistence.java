package org.commonprovenance.framework.store.persistence;

import java.util.UUID;

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
  Mono<Document> getById(@NotNull UUID identifier);

  @NotNull
  Mono<Void> deleteById(@NotNull UUID identifier);
}
