package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentController {
  Mono<DocumentResponseDTO> createProvDocument(DocumentFormDTO body);

  Flux<DocumentResponseDTO> getAllProvDocuments();

  Mono<DocumentResponseDTO> getProvDocumentById(String uuid);
}
