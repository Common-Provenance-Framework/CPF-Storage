package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationClient {

  Function<Organization, Mono<Organization>> create(Optional<String> trustedPartyUrl);

  Flux<Organization> getAll(Optional<String> trustedPartyUrl);

  Function<String, Mono<Organization>> getById(Optional<String> trustedPartyUrl);
}
