package org.commonprovenance.framework.store.persistence.neo4j;

import java.util.Objects;
import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.persistence.DocumentRepository;
import org.commonprovenance.framework.store.persistence.neo4j.mapper.DomainMapper;
import org.commonprovenance.framework.store.persistence.neo4j.mapper.EntityMapper;
import org.commonprovenance.framework.store.persistence.neo4j.repository.IDocumentNeo4jRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("neo4j")
@Repository
public class DocumentNeo4jRepository implements DocumentRepository {

  private final IDocumentNeo4jRepository documentRepository;

  public DocumentNeo4jRepository(
      IDocumentNeo4jRepository documentRepository) {
    this.documentRepository = documentRepository;
  }

  @Override
  @NotNull
  public Mono<Document> create(@NotNull Document document) {
    return document == null
        ? Mono.error(new InternalApplicationException(
            "DocumentNeo4jRepository - Error while creating document",
            new IllegalArgumentException(
                "Document can not be 'null'!")))
        : EntityMapper.toEntity(document)
            .flatMap(documentRepository::save)
            .onErrorResume(ex -> Mono.error(new InternalApplicationException(
                "DocumentNeo4jRepository - Error while creating new Document",
                ex)))
            .flatMap(DomainMapper::toDomain);
  }

  @Override
  @NotNull
  public Flux<Document> getAll() {
    return documentRepository.findAll()
        .onErrorResume(ex -> Flux.error(new InternalApplicationException(
            "DocumentNeo4jRepository - Error while reading documents",
            ex)))
        .flatMap(DomainMapper::toDomain);
  }

  @Override
  @NotNull
  public Mono<Document> getById(@NotNull UUID identifier) {
    try {
      return identifier == null
          ? Mono.error(new InternalApplicationException(
              "DocumentNeo4jRepository - Error while reading document",
              new IllegalArgumentException(
                  "Identifier can not be 'null'!")))
          : documentRepository.findById(Objects.requireNonNull(
              identifier.toString(),
              "Identifier can not be 'null'!"))
              .onErrorResume(ex -> Mono.error(new InternalApplicationException(
                  "DocumentNeo4jRepository - Error while reading document",
                  ex)))
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
      return identifier == null
          ? Mono.error(new InternalApplicationException(
              "DocumentNeo4jRepository - Error while deleting document",
              new IllegalArgumentException(
                  "Identifier can not be 'null'!")))
          : documentRepository.deleteById(Objects.requireNonNull(
              identifier.toString(),
              "Identifier can not be 'null'!"));
    } catch (NullPointerException nullPointerException) {
      return Mono.error(new InternalApplicationException("Wrong identifier!", nullPointerException));
    } catch (Exception exception) {
      return Mono.error(new InternalApplicationException(exception));
    }
  }
}
