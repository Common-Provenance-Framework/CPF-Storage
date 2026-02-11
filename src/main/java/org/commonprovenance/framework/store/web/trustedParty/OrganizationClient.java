package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationClient {
  @NotNull
  Function<Organization, Mono<Organization>> create(Optional<String> trustedPartyUrl);

  @NotNull
  Flux<Organization> getAll(Optional<String> trustedPartyUrl);

  @NotNull
  Function<String, Mono<Organization>> getById(Optional<String> trustedPartyUrl);
}
