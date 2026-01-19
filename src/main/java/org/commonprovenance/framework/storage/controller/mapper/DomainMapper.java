package org.commonprovenance.framework.storage.controller.mapper;

import java.util.Optional;
import java.util.UUID;

import org.commonprovenance.framework.storage.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.storage.exceptions.InternalApplicationException;
import org.commonprovenance.framework.storage.model.Document;
import org.commonprovenance.framework.storage.model.Format;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public class DomainMapper {
  @NotNull
  public static Mono<Document> toDomain(@NotNull DocumentFormDTO document) {
    if (document == null)
      return Mono.error(new InternalApplicationException(
          "Can not convert to Document",
          new IllegalArgumentException("DocumentFormDTO can not be null!")));

    Optional<Format> optFormat = Format.from(document.getFormat());
    if (optFormat.isEmpty())
      return Mono.error(new InternalApplicationException(
          "Can not convert to Document",
          new IllegalArgumentException("Unsupported format: " + document.getFormat())));

    return Mono.just(new Document(
        UUID.randomUUID(),
        document.getGraph(),
        optFormat.get()));
  }
}
