package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface TrustedPartyNeo4jRepositoryClient extends ReactiveNeo4jRepository<TrustedPartyNode, String> {
  Mono<TrustedPartyNode> findByName(String name);

  // Find the default trusted party (where isDefault = true)
  Mono<TrustedPartyNode> findByIsDefaultTrue();
}
