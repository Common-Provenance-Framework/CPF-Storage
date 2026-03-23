package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DocumentNeo4jRepositoryClient extends ReactiveNeo4jRepository<DocumentNode, String> {
  Flux<DocumentNode> findByIdentifier(String identifier);

  Mono<Void> deleteByIdentifier(String identifier);
}
