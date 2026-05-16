package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface TrustedPartyPersistence {

  Mono<Void> create(TrustedParty trustedParty);

  Mono<TrustedParty> getByName(String name);

  Mono<TrustedParty> getDefault();

  Mono<TrustedParty> getByOrganizationIdentifier(String organizationIdentifier);

  Mono<String> getUrlByOrganizationIdentifier(String organizationIdentifier);
}
