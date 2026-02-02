package org.commonprovenance.framework.store.persistence.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.entity.OrganizationEntity;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface OrganizationNeo4jRepositoryClient extends ReactiveNeo4jRepository<OrganizationEntity, String> {
  Mono<OrganizationEntity> findByName(String name);
}
