package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationClient {
  @NotNull
  Mono<Organization> create(
      @NotNull Organization organization,
      Optional<String> trustedPartyUrl);

  @NotNull
  Flux<Organization> getAll(Optional<String> trustedPartyUrl);

  @NotNull
  Mono<Organization> getById(
      @NotNull String organizationId,
      Optional<String> trustedPartyUrl);

  @NotNull
  Mono<Void> deleteById(
      @NotNull String organizationId,
      Optional<String> trustedPartyUrl);
}
