package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent;

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
  Mono<Organization> getOrganizationByIdentifier(@NotNull String identifier);

  @NotNull
  Mono<Void> deleteOrganizationByIdentifier(@NotNull String identifier);
}
