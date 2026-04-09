
package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TrustedPartyRepository {
  Mono<TrustedPartyNode> save(TrustedPartyNode entity);

  Flux<TrustedPartyNode> findAll();

  Mono<TrustedPartyNode> findByName(String name);

  Mono<TrustedPartyNode> findDefault();

  Mono<TrustedPartyNode> findByOrganizationIdentifier(String organizationIdentifier);

}
