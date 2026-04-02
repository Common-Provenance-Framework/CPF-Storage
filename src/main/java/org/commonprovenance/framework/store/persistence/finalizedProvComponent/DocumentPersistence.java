package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Document;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentPersistence {

  Mono<Document> create(Document document);

  Flux<Document> getAll();

  Mono<Document> getByIdentifier(String identifier);

  Mono<Boolean> existsByIdentifier(String identifier);

}
