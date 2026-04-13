package org.commonprovenance.framework.store.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentPersistence;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.DocumentRepository;
import org.springframework.stereotype.Component;

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
  public Mono<Document> create(Document document) {
    return MONO.<Document>makeSureNotNullWithMessage("Document can not be 'null'!").apply(document)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("DocumentPersistence - Error while creating new Document"))
        .map(ModelFactory::toDomain);
  }

  @Override
  public Flux<Document> getAll() {
    return repository.findAll()
        .onErrorResume(MONO.exceptionWrapper("DocumentPersistence - Error while reading Documents"))
        .map(ModelFactory::toDomain);
  }

  @Override
  public Mono<Document> getByIdentifier(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Document identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::findByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("DocumentPersistence - Error while reading Document"))
        .map(ModelFactory::toDomain)
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Document with identifier '" + identifier + "' has not been found!"))));
  }

  @Override
  public Mono<Boolean> existsByIdentifier(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Document identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::existsByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("DocumentPersistence - Error while reading Document"));
  }

  @Override
  public Mono<String> getOrganizationIdentifierByIdentifier(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Document identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::getOrganizationIdentifierByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("DocumentPersistence - Error while reading Document"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Document with identifier '" + identifier + "' has not been found!"))));
  }

}
