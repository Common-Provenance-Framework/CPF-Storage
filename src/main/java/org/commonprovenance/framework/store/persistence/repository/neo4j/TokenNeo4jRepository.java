package org.commonprovenance.framework.store.persistence.repository.neo4j;

import org.commonprovenance.framework.store.persistence.entity.TokenEntity;
import org.commonprovenance.framework.store.persistence.repository.TokenRepository;
import org.commonprovenance.framework.store.persistence.repository.neo4j.client.TokenNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class TokenNeo4jRepository implements TokenRepository {
  private final TokenNeo4jRepositoryClient client;

  public TokenNeo4jRepository(
      TokenNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<TokenEntity> save(TokenEntity entity) {
    return client.save(entity);
  }

  @Override
  public Flux<TokenEntity> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<TokenEntity> findById(String id) {
    return client.findById(id);
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return client.deleteById(id);
  }
}
