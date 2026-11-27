
package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface TrustedPartyRepository {
  Mono<Void> save(TrustedParty trustedParty);

  Mono<TrustedParty> findByName(String name);

  Mono<TrustedParty> findDefault();

  Mono<TrustedParty> findByOrganizationIdentifier(String organizationIdentifier);

  Mono<String> findUrlByOrganizationIdentifier(String organizationIdentifier);

}
