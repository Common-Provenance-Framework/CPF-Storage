package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Document;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentService {
  @NotNull
  Mono<Document> storeDocument(@NotNull Document document);

  @NotNull
  Flux<Document> getAllDocuments();

  @NotNull
  Mono<Document> getDocumentById(@NotNull String id);

  @NotNull
  Mono<Void> deleteDocumentById(@NotNull String id);
}
