package org.commonprovenance.framework.store.web.trustedParty;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationClient {
  @NotNull
  Mono<Organization> create(@NotNull Organization organization);

  @NotNull
  Flux<Organization> getAll();

  @NotNull
  Mono<Organization> getById(@NotNull UUID id);

  @NotNull
  Mono<Organization> getByName(@NotNull String name);

  @NotNull
  Mono<Void> deleteById(@NotNull UUID id);
}
