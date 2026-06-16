package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Document;

import reactor.core.publisher.Mono;

public interface DocumentRepository {
  Mono<Void> save(Document document);

  Mono<Document> findByIdentifier(String identifier);

  Mono<Boolean> existsByIdentifier(String identifier);

  Mono<String> getOrganizationIdentifierByIdentifier(String identifier);

}
