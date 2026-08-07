package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;

import reactor.core.publisher.Mono;

public interface MetaDocumentController {
  Mono<Void> exists(String uuid);

  Mono<DocumentResponseDTO> getMetaDocument(String uuid);
}
