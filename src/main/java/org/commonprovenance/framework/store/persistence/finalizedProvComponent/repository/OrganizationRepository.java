package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationRepository {
  Mono<OrganizationNode> save(OrganizationNode entity);

  Flux<OrganizationNode> findAll();

  Mono<OrganizationNode> findById(String id);

  Mono<OrganizationNode> findByName(String name);

  Mono<Void> deleteById(String id);
}
