package org.commonprovenance.framework.store.controller.dto.response.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.model.Document;

import reactor.core.publisher.Mono;

public class DTOFactory {
  private static DocumentResponseDTO fromModel(Document model) {
    return new DocumentResponseDTO(
        model.getId().toString(),
        model.getGraph(),
        model.getFormat().toString());
  }

  public static Mono<DocumentResponseDTO> toDTO(Document document) {
    return MONO.makeSureNotNull(document)
        .map(DTOFactory::fromModel);
  }
}
