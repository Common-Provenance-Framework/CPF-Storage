package org.commonprovenance.framework.storage.service;

import jakarta.validation.constraints.NotNull;

import org.commonprovenance.framework.storage.model.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentService {
  @NotNull
  Mono<Document> storeDocument(@NotNull Document document);

  @NotNull
  Flux<Document> getAllDocuments();

  @NotNull
  Mono<Document> getDocumentById(@NotNull java.util.UUID identifier);

  @NotNull
  Mono<Void> deleteDocumentById(@NotNull java.util.UUID identifier);
}
