package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationWeb {

  Mono<Void> create(Organization organization);

  Flux<Organization> getAll(Optional<String> optTrustedPartyBaseUrl);

  Mono<Organization> getById(Organization organization);
}
