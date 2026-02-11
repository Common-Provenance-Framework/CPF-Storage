package org.commonprovenance.framework.store.persistence;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.constraints.NotNull;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationPersistence {
  @NotNull
  Mono<Organization> create(@NotNull Organization document);

  @NotNull
  Mono<Organization> update(@NotNull Organization document);

  @NotNull
  Flux<Organization> getAll();

  @NotNull
  Mono<Organization> getById(@NotNull UUID id);

  @NotNull
  Mono<Organization> getByName(@NotNull String name);

  @NotNull
  Mono<Void> deleteById(@NotNull UUID id);
}
