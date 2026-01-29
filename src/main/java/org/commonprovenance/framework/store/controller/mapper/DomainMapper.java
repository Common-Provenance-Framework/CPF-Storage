package org.commonprovenance.framework.store.controller.mapper;

import java.util.Optional;
import java.util.UUID;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;

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

  @NotNull
  public static Mono<UUID> toDomain(@NotNull String uuidString) {
    if (uuidString == null)
      return Mono.error(new InternalApplicationException(
          "Can not convert to UUID",
          new IllegalArgumentException("UUID String can not be null!")));
    try {
      return Mono.just(UUID.fromString(uuidString));
    } catch (IllegalArgumentException illegalArgumentException) {
      return Mono.error(new InternalApplicationException(
          "Can not convert to UUID",
          new IllegalArgumentException("Not valid UUID string: " + uuidString)));
    } catch (Exception exception) {
      return Mono.error(new InternalApplicationException(exception));
    }
  }
}
