package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.resolver.annotation.LoadMetaDocument;
import org.commonprovenance.framework.store.model.MetaDocument;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface MetaDocumentController {
  Mono<Void> exists(String uuid);

  Mono<DocumentResponseDTO> getMetaDocument(
      @NotNull @LoadMetaDocument MetaDocument metaDocument);
}
