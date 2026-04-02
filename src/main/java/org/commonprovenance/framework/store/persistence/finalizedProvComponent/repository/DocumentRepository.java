package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentRepository {
  Mono<DocumentNode> save(DocumentNode document);

  Flux<DocumentNode> findAll();

  Mono<DocumentNode> findByIdentifier(String identifier);

  Mono<Boolean> existsByIdentifier(String identifier);

}
