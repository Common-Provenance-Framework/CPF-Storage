package org.commonprovenance.framework.store.persistence.neo4j.mapper;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.persistence.neo4j.entities.DocumentEntity;

import reactor.core.publisher.Mono;

public class DomainMapper {
  public static Mono<Document> toDomain(DocumentEntity entity) {
    try {
      UUID identifier = UUID.fromString(entity.getIdentifier());
      Format format = Format.from(entity.getFormat()).get();
      return Mono.just(new Document(identifier, entity.getGraph(), format));
    } catch (NoSuchElementException noSuchElementException) {
      return Mono.error(new InternalApplicationException(
          "Format '" + entity.getFormat() + "' is not valid format string.",
          noSuchElementException));
    } catch (IllegalArgumentException illegalArgumentException) {
      return Mono.error(new InternalApplicationException(
          "Identifier '" + entity.getIdentifier() + "' is not valid UUID string.",
          illegalArgumentException));
    } catch (Exception exception) {
      return Mono.error(new InternalApplicationException(exception));
    }
  }
}
