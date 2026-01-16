package org.commonprovenance.framework.storage.service.impl;

import org.commonprovenance.framework.storage.model.Document;
import org.commonprovenance.framework.storage.persistence.DocumentRepository;
import org.commonprovenance.framework.storage.service.DocumentService;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class DocumentServiceImpl implements DocumentService {
  private final DocumentRepository documentRepository;

  public DocumentServiceImpl(DocumentRepository documentRepository) {
    this.documentRepository = documentRepository;
  }

  @NotNull
  public Mono<Document> storeDocument(@NotNull Document document) {
    return this.documentRepository.create(document);
  }

  @NotNull
  public Flux<Document> getAllDocuments() {
    return this.documentRepository.getAll();
  }

  @NotNull
  public Mono<Document> getDocumentById(@NotNull java.util.UUID identifier) {
    return this.documentRepository.getById(identifier);
  }

  @NotNull
  public Mono<Void> deleteDocumentById(@NotNull java.util.UUID identifier) {
    return this.documentRepository.deleteById(identifier);
  }
}