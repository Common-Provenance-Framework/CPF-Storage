package org.commonprovenance.framework.store.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentPersistence;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.DocumentRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Validated
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
        .flatMap(NodeFactory::toEntity)
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
  public Mono<Document> getByIdentifier(@NotNull String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Document identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::findByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading document"))
        .flatMap(ModelFactory::toDomain)
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Document with identifier '" + identifier + "' has not been found!"))));
  }

  @Override
  @NotNull
  public Mono<Void> deleteByIdentifier(@NotNull String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Document identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::deleteByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("DocumentNeo4jRepository - Error while reading document"));
  }
}
