package org.commonprovenance.framework.store.persistence;

import java.util.UUID;

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
  Mono<Token> getById(@NotNull UUID id);

  @NotNull
  Mono<Void> deleteById(@NotNull UUID id);
}
