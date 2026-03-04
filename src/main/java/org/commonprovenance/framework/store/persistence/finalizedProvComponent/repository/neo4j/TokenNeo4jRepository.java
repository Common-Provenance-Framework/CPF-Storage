package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TokenRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client.TokenNeo4jRepositoryClient;
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
  public Mono<TokenNode> save(TokenNode entity) {
    return client.save(entity);
  }

  @Override
  public Flux<TokenNode> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<TokenNode> findById(String id) {
    return client.findById(id);
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return client.deleteById(id);
  }
}
