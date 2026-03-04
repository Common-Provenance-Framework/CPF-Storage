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

import jakarta.validation.constraints.NotNull;
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
  @NotNull
  public Mono<Document> create(@NotNull Document bundle) {
    return MONO.<Document>makeSureNotNullWithMessage("Bundle can not be 'null'!").apply(bundle)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("BundleNeo4jRepository - Error while creating new Bundle"))
        .flatMap(ProvenanceFactory.bundleToProv(this.configuration));
  }

  @Override
  @NotNull
  public Mono<Document> getById(@NotNull String id) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle Id can not be 'null'!").apply(id)
        .flatMap(repository::findById)
        .onErrorResume(MONO.exceptionWrapper("BundleNeo4jRepository - Error while reading bundle"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Bundle with id '" + id + "' has not been found!"))))
        .flatMap(ProvenanceFactory.bundleToProv(configuration));
  }

  @Override
  public @NotNull Mono<Boolean> exists(@NotNull String id) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle Id can not be 'null'!").apply(id)
        .flatMap(repository::exists)
        .onErrorResume(MONO.exceptionWrapper("BundleNeo4jRepository - Error while checking Bundle"));
  }

}
