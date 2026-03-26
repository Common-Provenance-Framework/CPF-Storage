package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TrustedPartyRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client.TrustedPartyNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class TrustedPartyNeo4jRepository implements TrustedPartyRepository {
  private final TrustedPartyNeo4jRepositoryClient client;

  public TrustedPartyNeo4jRepository(
      TrustedPartyNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<TrustedPartyNode> save(TrustedPartyNode entity) {
    return client.save(entity);
  }

  @Override
  public Flux<TrustedPartyNode> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<TrustedPartyNode> findByName(String name) {
    return client.findByName(name);
  }

  @Override
  public Mono<TrustedPartyNode> findDefault() {
    return client.findDefault();
  }
}
