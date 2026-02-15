package org.commonprovenance.framework.store.persistence.repository.neo4j;

import org.commonprovenance.framework.store.persistence.entity.TrustedPartyEntity;
import org.commonprovenance.framework.store.persistence.repository.TrustedPartyRepository;
import org.commonprovenance.framework.store.persistence.repository.neo4j.client.TrustedPartyNeo4jRepositoryClient;
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
  public Mono<TrustedPartyEntity> save(TrustedPartyEntity entity) {
    return client.save(entity);
  }

  @Override
  public Flux<TrustedPartyEntity> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<TrustedPartyEntity> findById(String id) {
    return client.findById(id);
  }

  @Override
  public Mono<TrustedPartyEntity> findByName(String name) {
    return client.findByName(name);
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return client.deleteById(id);
  }

  @Override
  public Mono<TrustedPartyEntity> findDefault() {
    return client.findByIsDefaultTrue();
  }
}
