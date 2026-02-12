package org.commonprovenance.framework.store.persistence;

import java.util.UUID;

import org.commonprovenance.framework.store.model.TrustedParty;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrustedPartyPersistence {
  @NotNull
  Mono<TrustedParty> create(@NotNull TrustedParty trustedParty);

  @NotNull
  Mono<TrustedParty> update(@NotNull TrustedParty trustedParty);

  @NotNull
  Flux<TrustedParty> getAll();

  @NotNull
  Mono<TrustedParty> getById(@NotNull UUID id);

  @NotNull
  Mono<TrustedParty> getByName(@NotNull String name);

  @NotNull
  Mono<Void> deleteById(@NotNull UUID id);
}
