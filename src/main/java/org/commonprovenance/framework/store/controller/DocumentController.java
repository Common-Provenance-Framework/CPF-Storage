package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentController {
  Mono<DocumentResponseDTO> createProvDocument(@Valid @NotNull DocumentFormDTO body);

  Flux<DocumentResponseDTO> getAllProvDocuments();

  Mono<DocumentResponseDTO> getProvDocumentById(String uuid);

  Mono<Void> exists(String uuid);
}
