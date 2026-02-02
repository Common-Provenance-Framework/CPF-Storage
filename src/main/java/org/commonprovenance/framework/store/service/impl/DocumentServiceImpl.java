package org.commonprovenance.framework.store.service.impl;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.persistence.DocumentPersistence;
import org.commonprovenance.framework.store.service.DocumentService;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DocumentServiceImpl implements DocumentService {
  private final DocumentPersistence documentPersistence;

  public DocumentServiceImpl(DocumentPersistence documentPersistence) {
    this.documentPersistence = documentPersistence;
  }

  @NotNull
  public Mono<Document> storeDocument(@NotNull Document document) {
    return this.documentPersistence.create(document);
  }

  @NotNull
  public Flux<Document> getAllDocuments() {
    return this.documentPersistence.getAll();
  }

  @NotNull
  public Mono<Document> getDocumentById(@NotNull java.util.UUID identifier) {
    return this.documentPersistence.getById(identifier);
  }

  @NotNull
  public Mono<Void> deleteDocumentById(@NotNull java.util.UUID identifier) {
    return this.documentPersistence.deleteById(identifier);
  }
}