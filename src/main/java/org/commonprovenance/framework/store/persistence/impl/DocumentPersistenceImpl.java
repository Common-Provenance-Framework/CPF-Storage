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

  private final DocumentRepository documentRepository;

  public DocumentPersistenceImpl(
      DocumentRepository documentRepository) {
    this.documentRepository = documentRepository;
  }

  @Override
  @NotNull
  public Mono<Document> create(@NotNull Document document) {
    return MONO.<Document>makeSureNotNullWithMessage("Document can not be 'null'!").apply(document)
        .flatMap(EntityFactory::toEntity)
        .flatMap(documentRepository::save)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while creating new Document"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Flux<Document> getAll() {
    return documentRepository.findAll()
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading documents"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Mono<Document> getById(@NotNull UUID identifier) {
    return MONO.<UUID>makeSureNotNullWithMessage("Identifier can not be 'null'!").apply(identifier)
        .map(UUID::toString)
        .flatMap(documentRepository::findById)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading document"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Mono<Void> deleteById(@NotNull UUID identifier) {
    return MONO.<UUID>makeSureNotNullWithMessage("Identifier can not be 'null'!").apply(identifier)
        .map(UUID::toString)
        .flatMap(documentRepository::deleteById)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading document"));
  }
}
