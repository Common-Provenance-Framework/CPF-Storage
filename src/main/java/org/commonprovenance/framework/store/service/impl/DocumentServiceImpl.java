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
  private final DocumentPersistence persistence;

  public DocumentServiceImpl(DocumentPersistence persistence) {
    this.persistence = persistence;
  }

  @NotNull
  public Mono<Document> storeDocument(@NotNull Document document) {
    return this.persistence.create(document);
  }

  @NotNull
  public Flux<Document> getAllDocuments() {
    return this.persistence.getAll();
  }

  @NotNull
  public Mono<Document> getDocumentById(@NotNull java.util.UUID id) {
    return this.persistence.getById(id);
  }

  @NotNull
  public Mono<Void> deleteDocumentById(@NotNull java.util.UUID id) {
    return this.persistence.deleteById(id);
  }
}