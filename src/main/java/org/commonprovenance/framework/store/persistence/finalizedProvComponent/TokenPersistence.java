package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Token;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenPersistence {
  @NotNull
  Mono<Token> create(@NotNull Token document);

  @NotNull
  Flux<Token> getAll();

  @NotNull
  Mono<Token> getById(@NotNull String id);

  @NotNull
  Mono<Void> deleteById(@NotNull String id);
}
