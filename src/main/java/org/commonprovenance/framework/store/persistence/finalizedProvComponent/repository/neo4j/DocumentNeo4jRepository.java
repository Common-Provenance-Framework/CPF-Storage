package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.DocumentRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client.DocumentNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class DocumentNeo4jRepository implements DocumentRepository {
  private final DocumentNeo4jRepositoryClient client;

  public DocumentNeo4jRepository(
      DocumentNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<DocumentNode> save(DocumentNode entity) {
    return client.save(entity);
  }

  @Override
  public Flux<DocumentNode> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<DocumentNode> findById(String id) {
    return client.findById(id);
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return client.deleteById(id);
  }
}
