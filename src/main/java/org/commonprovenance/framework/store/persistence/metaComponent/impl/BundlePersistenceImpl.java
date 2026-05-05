package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import java.util.function.Function;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.ProvenanceFactory;
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
  public Mono<Void> create(String identifier) {
    return Mono.just(identifier)
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
  public Function<String, Mono<Void>> addToken(String identifier) {
    return (String jwtToken) -> Mono.just(jwtToken)
        .flatMap(metaBundleRepository.addToken(identifier));
  }

  @Override
  public Mono<Document> getByIdentifier(String identifier) {
    return Mono.just(identifier)
        .flatMap(metaBundleRepository::findByIdentifier)
        .flatMap(ProvenanceFactory.bundleToProv(configuration));
  }

  @Override
  public Mono<Boolean> exists(String identifier) {
    return Mono.just(identifier)
        .flatMap(metaBundleRepository::existsByIdentifier);
  }

  @Override
  public Mono<Boolean> notExists(String identifier) {
    return this.exists(identifier).map(exists -> !exists);
  }

}
