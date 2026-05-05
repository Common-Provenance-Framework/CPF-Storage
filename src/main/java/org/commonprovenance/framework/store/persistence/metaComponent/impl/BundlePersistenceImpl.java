package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import java.util.function.Function;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.NodeToProvFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.EntityRepository;
import org.openprovenance.prov.model.Document;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class BundlePersistenceImpl implements BundlePersistence {

  private final BundleRepository metaBundleRepository;
  private final EntityRepository entityRepository;
  private final AppConfiguration configuration;

  public BundlePersistenceImpl(
      BundleRepository metaBundleRepository,
      EntityRepository entityRepository,
      AppConfiguration configuration) {
    this.metaBundleRepository = metaBundleRepository;
    this.entityRepository = entityRepository;
    this.configuration = configuration;
  }

  @Override
  public Mono<Void> create(String metaBundleIdentifier) {
    return Mono.just(metaBundleIdentifier)
        .flatMap(metaBundleRepository::create);
  }

  @Override
  public Function<String, Mono<Void>> addVersionEntity(String metaBundleIdentifier) {
    return (String versionEntityIdentifier) -> metaBundleRepository.hasVersionEntity(metaBundleIdentifier)
        .flatMap(hasVersionEntity -> hasVersionEntity
            ? entityRepository.addVersion(metaBundleIdentifier, versionEntityIdentifier)
            : entityRepository.addFirstVersion(metaBundleIdentifier, versionEntityIdentifier))
        .delayUntil(entityRepository.makeSpecializationOfGeneralVersion(metaBundleIdentifier))
        .delayUntil(entityRepository.addToBundle(metaBundleIdentifier))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Version has not been added into meta component provenance!")));
  }

  @Override
  public Function<String, Mono<Void>> addToken(String metaBundleIdentifier) {
    return (String jwtToken) -> metaBundleRepository.addToken(metaBundleIdentifier, jwtToken)
        .delayUntil(metaBundleRepository.addTokenToMetaBundle(metaBundleIdentifier))
        .delayUntil(metaBundleRepository.addTokenGenerationToBundle(metaBundleIdentifier))
        .delayUntil(metaBundleRepository.addTokenGeneratorToBundle(metaBundleIdentifier))
        .then();
  }

  @Override
  public Mono<Document> getByIdentifier(String metaBundleIdentifier) {
    return Mono.just(metaBundleIdentifier)
        .flatMap(metaBundleRepository::findByIdentifier)
        .flatMap(NodeToProvFactory.bundleToProv(configuration));
  }

  @Override
  public Mono<Boolean> exists(String metaBundleIdentifier) {
    return Mono.just(metaBundleIdentifier)
        .flatMap(metaBundleRepository::existsByIdentifier);
  }

  @Override
  public Mono<Boolean> notExists(String metaBundleIdentifier) {
    return this.exists(metaBundleIdentifier).map(exists -> !exists);
  }

}
