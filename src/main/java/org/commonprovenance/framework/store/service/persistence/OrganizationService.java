package org.commonprovenance.framework.store.service.persistence;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationService {
  @NotNull
  Mono<Organization> storeOrganization(@NotNull Organization organization);

  @NotNull
  Mono<Organization> updateOrganization(@NotNull Organization organization);

  @NotNull
  Mono<Boolean> exists(@NotNull Organization organization);

  @NotNull
  Mono<Boolean> notExists(@NotNull Organization organization);

  @NotNull
  Flux<Organization> getAllOrganizations();

  @NotNull
  Mono<Organization> getOrganizationById(@NotNull UUID id);

  @NotNull
  Mono<Organization> getOrganizationByName(@NotNull String name);

  @NotNull
  Mono<Void> deleteOrganizationById(@NotNull UUID id);
}
