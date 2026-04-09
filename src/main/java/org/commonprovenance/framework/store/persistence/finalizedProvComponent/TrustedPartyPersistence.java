package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrustedPartyPersistence {

  Mono<TrustedParty> create(TrustedParty trustedParty);

  Mono<TrustedParty> update(TrustedParty trustedParty);

  Flux<TrustedParty> getAll();

  Mono<TrustedParty> getByName(String name);

  Mono<TrustedParty> getDefault();

  Mono<TrustedParty> getByOrganizationIdentifier(String organizationIdentifier);

  Mono<String> getUrlByOrganizationIdentifier(String organizationIdentifier);
}
