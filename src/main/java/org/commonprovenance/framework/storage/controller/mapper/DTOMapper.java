package org.commonprovenance.framework.storage.controller.mapper;

import org.commonprovenance.framework.storage.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.storage.exceptions.InternalApplicationException;
import org.commonprovenance.framework.storage.model.Document;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public class DTOMapper {
  @NotNull
  public static Mono<DocumentResponseDTO> toDTO(@NotNull Document domain) {
    return domain == null
        ? Mono.error(new InternalApplicationException(
            "Can not convert to DocumentResponseDTO",
            new IllegalArgumentException("Document can not be null!")))
        : Mono.just(new DocumentResponseDTO(
            domain.getIdentifier().toString(),
            domain.getGraph(),
            domain.getFormat().toString()));
  }
}
