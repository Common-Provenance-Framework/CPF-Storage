package org.commonprovenance.framework.store.persistence.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.DocumentPersistence;
import org.commonprovenance.framework.store.persistence.entity.factory.EntityFactory;
import org.commonprovenance.framework.store.persistence.repository.DocumentRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DocumentPersistenceImpl implements DocumentPersistence {

  private final DocumentRepository repository;

  public DocumentPersistenceImpl(
      DocumentRepository repository) {
    this.repository = repository;
  }

  @Override
  @NotNull
  public Mono<Document> create(@NotNull Document document) {
    return MONO.<Document>makeSureNotNullWithMessage("Document can not be 'null'!").apply(document)
        .flatMap(EntityFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while creating new Document"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Flux<Document> getAll() {
    return repository.findAll()
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading documents"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Mono<Document> getById(@NotNull UUID uuid) {
    return MONO.<UUID>makeSureNotNullWithMessage("Document Id can not be 'null'!").apply(uuid)
        .map(UUID::toString)
        .flatMap(repository::findById)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading document"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Mono<Void> deleteById(@NotNull UUID uuid) {
    return MONO.<UUID>makeSureNotNullWithMessage("Document Id can not be 'null'!").apply(uuid)
        .map(UUID::toString)
        .flatMap(repository::deleteById)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading document"));
  }
}
