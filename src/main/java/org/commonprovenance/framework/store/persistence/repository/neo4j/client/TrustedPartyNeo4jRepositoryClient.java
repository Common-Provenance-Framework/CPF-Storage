package org.commonprovenance.framework.store.persistence.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.entity.TrustedPartyEntity;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface TrustedPartyNeo4jRepositoryClient extends ReactiveNeo4jRepository<TrustedPartyEntity, String> {
  Mono<TrustedPartyEntity> findByName(String name);
}
