package org.commonprovenance.framework.store.persistence.repository.neo4j;

import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.commonprovenance.framework.store.persistence.repository.DocumentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class DocumentNeo4jRepository implements DocumentRepository {
  private final IDocumentNeo4jRepository repository;

  public DocumentNeo4jRepository(
      IDocumentNeo4jRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<DocumentEntity> save(DocumentEntity entity) {
    return repository.save(entity);
  }

  @Override
  public Flux<DocumentEntity> findAll() {
    return repository.findAll();
  }

  @Override
  public Mono<DocumentEntity> findById(String id) {
    return repository.findById(id);
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return repository.deleteById(id);
  }
}
