package org.commonprovenance.framework.store.controller.facade;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.model.MetaDocument;

import reactor.core.publisher.Mono;

public interface MetaDocumentFacade {
  Mono<Void> exists(String identifier);

  Mono<DocumentResponseDTO> getMetaDocument(MetaDocument metaDocument);

}
