package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface DocumentService {

  Mono<Void> storeDocument(Document document);

  Mono<Document> getDocumentByIdentifier(String identifier);

  Mono<Boolean> existsByIdentifier(String identifier);

  Mono<Boolean> exists(Document document);

  Mono<Boolean> notExists(Document document);

  Mono<Void> checkDocumentDoesNotExists(Organization organization);

  Mono<String> getOrganizationIdentifierByIdentifier(String identifier);

  Mono<Void> checkSpecForwardConnectorsResolvable(Document docuement);

  Mono<Void> checkBackwardConnectorsResolvable(Document docuement);

}
