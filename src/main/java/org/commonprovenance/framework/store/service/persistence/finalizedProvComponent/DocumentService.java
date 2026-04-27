package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Document;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentService {

  Mono<Document> storeDocument(Document document);

  Flux<Document> getAllDocuments();

  Mono<Document> getDocumentByIdentifier(String identifier);

  Mono<Boolean> existsByIdentifier(String identifier);

  Mono<Boolean> exists(Document document);

  Mono<Boolean> notExists(Document document);

  Mono<Void> checkDocumentDoesNotExists(Document document);

  Mono<String> getOrganizationIdentifierByIdentifier(String identifier);

  Mono<Void> checkSpecForwardConnectorsResolvable(Document docuement);

  Mono<Void> checkBackwardConnectorsResolvable(Document docuement);

}
