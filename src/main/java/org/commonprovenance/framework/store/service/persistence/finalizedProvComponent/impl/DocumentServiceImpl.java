package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentPersistence;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.DocumentService;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DocumentServiceImpl implements DocumentService {
  private final DocumentPersistence persistence;

  public DocumentServiceImpl(DocumentPersistence persistence) {
    this.persistence = persistence;
  }

  @Override
  public Mono<Document> storeDocument(Document document) {
    return this.persistence.create(document);
  }

  @Override
  public Flux<Document> getAllDocuments() {
    return this.persistence.getAll();
  }

  @Override
  public Mono<Document> getDocumentByIdentifier(String identifier) {
    return this.persistence.getByIdentifier(identifier);
  }

}