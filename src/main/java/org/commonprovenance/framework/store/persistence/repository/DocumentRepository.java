package org.commonprovenance.framework.store.persistence.repository;

import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentRepository {
  Mono<DocumentEntity> save(DocumentEntity entity);

  Flux<DocumentEntity> findAll();

  Mono<DocumentEntity> findById(String id);

  Mono<Void> deleteById(String id);
}
