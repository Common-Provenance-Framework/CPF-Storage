package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationWeb {

  Function<Organization, Mono<Void>> create(Optional<String> optTrustedPartyUrl);

  Flux<Organization> getAll(Optional<String> optTrustedPartyUrl);

  Function<String, Mono<Organization>> getById(Optional<String> optTrustedPartyUrl);
}
