package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentRepository {
  Mono<DocumentNode> save(DocumentNode entity);

  Flux<DocumentNode> findAll();

  Mono<DocumentNode> findById(String id);

  Mono<Void> deleteById(String id);
}
