package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

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
  Mono<Organization> getByIdentifier(@NotNull String identifier);

  @NotNull
  Mono<Void> deleteByIdentifier(@NotNull String identifier);
}
