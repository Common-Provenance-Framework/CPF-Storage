package org.commonprovenance.framework.store.controller;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface DocumentController {
  Mono<TokenResponseDTO> createProvDocument(@Valid @NotNull DocumentFormDTO body);

  Mono<DocumentResponseDTO> getProvDocumentById(String uuid);

  Mono<Void> exists(String uuid);
}
