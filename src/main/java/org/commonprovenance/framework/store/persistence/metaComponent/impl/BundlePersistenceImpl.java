package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.ProvenanceFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.openprovenance.prov.model.Document;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class BundlePersistenceImpl implements BundlePersistence {

  private final BundleRepository repository;
  private final AppConfiguration configuration;

  public BundlePersistenceImpl(
      BundleRepository repository,
      AppConfiguration configuration) {
    this.repository = repository;
    this.configuration = configuration;
  }

  @Override
  public Mono<Document> create(Document document) {
    return MONO.<Document>makeSureNotNullWithMessage("Document can not be 'null'!").apply(document)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("BundlePersistence - Error while creating new Bundle"))
        .flatMap(ProvenanceFactory.bundleToProv(this.configuration));
  }

  @Override
  public Mono<Document> getByIdentifier(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::findByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("BundlePersistence - Error while reading bundle"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Bundle with identifier '" + identifier + "' has not been found!"))))
        .flatMap(ProvenanceFactory.bundleToProv(configuration));
  }

  @Override
  public Mono<Boolean> exists(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!").apply(identifier)
        .flatMap(repository::exists)
        .onErrorResume(MONO.exceptionWrapper("BundlePersistence - Error while checking Bundle"));
  }

}
