package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationRepository {
  Mono<OrganizationNode> save(OrganizationNode organization);

  Flux<OrganizationNode> findAll();

  Mono<OrganizationNode> findByIdentifier(String identifier);

}
