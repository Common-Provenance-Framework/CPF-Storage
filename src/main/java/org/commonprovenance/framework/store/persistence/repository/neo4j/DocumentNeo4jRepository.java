package org.commonprovenance.framework.store.persistence.repository.neo4j;

import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.commonprovenance.framework.store.persistence.repository.DocumentRepository;
import org.commonprovenance.framework.store.persistence.repository.neo4j.client.DocumentNeo4jRepositoryClient;
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
  public Mono<DocumentEntity> save(DocumentEntity entity) {
    return client.save(entity);
  }

  @Override
  public Flux<DocumentEntity> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<DocumentEntity> findById(String id) {
    return client.findById(id);
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return client.deleteById(id);
  }
}
