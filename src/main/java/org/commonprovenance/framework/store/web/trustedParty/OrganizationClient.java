package org.commonprovenance.framework.store.web.trustedParty;

import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationClient {
  @NotNull
  Mono<Boolean> create(@NotNull Organization organization);

  @NotNull
  Flux<Organization> getAll();

  @NotNull
  Mono<Organization> getById(@NotNull String id);

  @NotNull
  Mono<Void> deleteById(@NotNull String id);
}
