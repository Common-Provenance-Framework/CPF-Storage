package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface TrustedPartyService {

  Mono<TrustedParty> storeTrustedParty(TrustedParty trustedParty);

  Mono<TrustedParty> findTrustedParty(TrustedParty trustedParty);

  Mono<TrustedParty> getDefaultTrustedParty();

  Mono<TrustedParty> getTrustedPartyByName(String name);

  Mono<TrustedParty> getTrustedPartyByOrganizationIdentifier(String organizationIdentifier);
}
