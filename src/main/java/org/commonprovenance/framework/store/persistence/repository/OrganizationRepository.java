package org.commonprovenance.framework.store.persistence.repository;

import org.commonprovenance.framework.store.persistence.entity.OrganizationEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationRepository {
  Mono<OrganizationEntity> save(OrganizationEntity entity);

  Flux<OrganizationEntity> findAll();

  Mono<OrganizationEntity> findById(String id);

  Mono<OrganizationEntity> findByName(String name);

  Mono<Void> deleteById(String id);
}
