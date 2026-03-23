package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentPersistence;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.DocumentService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Validated
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
  public Mono<Document> getDocumentByIdentifier(@NotNull String identifier) {
    return this.persistence.getByIdentifier(identifier);
  }

  @NotNull
  public Mono<Void> deleteDocumentByIdentifier(@NotNull String identifier) {
    return this.persistence.deleteByIdentifier(identifier);
  }
}