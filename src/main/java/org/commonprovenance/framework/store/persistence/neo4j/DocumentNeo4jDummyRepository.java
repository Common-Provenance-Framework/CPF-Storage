package org.commonprovenance.framework.store.persistence.neo4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.persistence.DocumentRepository;
import org.commonprovenance.framework.store.persistence.neo4j.entities.DocumentEntity;
import org.commonprovenance.framework.store.persistence.neo4j.mapper.DomainMapper;
import org.commonprovenance.framework.store.persistence.neo4j.mapper.EntityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("neo4jdummy")
@Repository
public class DocumentNeo4jDummyRepository implements DocumentRepository {
  private static Map<String, DocumentEntity> documents = new HashMap<>();

  @Override
  @NotNull
  public Mono<Document> create(@NotNull Document document) {
    return EntityMapper.toEntity(document)
        .doOnNext(d -> documents.put(d.getIdentifier(), d))
        .flatMap(DomainMapper::toDomain);
  }

  @Override
  @NotNull
  public Flux<Document> getAll() {
    return Flux.fromIterable(documents.values())
        .flatMap(DomainMapper::toDomain);
  }

  @Override
  @NotNull
  public Mono<Document> getById(@NotNull UUID identifier) {
    try {
      return Mono.just(documents.get(Objects.requireNonNull(
          identifier.toString(),
          "Identifier can not be 'null'!")))
          .flatMap(DomainMapper::toDomain);
    } catch (NullPointerException nullPointerException) {
      return Mono.error(new InternalApplicationException("Wrong identifier!", nullPointerException));
    } catch (Exception exception) {
      return Mono.error(new InternalApplicationException(exception));
    }
  }

  @Override
  @NotNull
  public Mono<Void> deleteById(@NotNull UUID identifier) {
    try {
      return Mono.just(documents.remove(Objects.requireNonNull(
          identifier.toString(),
          "Identifier can not be 'null'!")))
          .then();
    } catch (NullPointerException nullPointerException) {
      return Mono.error(new InternalApplicationException("Wrong identifier!", nullPointerException));
    } catch (Exception exception) {
      return Mono.error(new InternalApplicationException(exception));
    }
  }
}
