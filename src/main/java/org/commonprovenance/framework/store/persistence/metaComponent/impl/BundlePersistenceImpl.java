package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.function.Function;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.NodeToProvFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.MetaBundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.EntityRepository;
import org.openprovenance.prov.model.Document;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class BundlePersistenceImpl implements BundlePersistence {

  private final MetaBundleRepository metaBundleRepository;
  private final EntityRepository entityRepository;
  private final AppConfiguration configuration;

  public BundlePersistenceImpl(
      MetaBundleRepository metaBundleRepository,
      EntityRepository entityRepository,
      AppConfiguration configuration) {
    this.metaBundleRepository = metaBundleRepository;
    this.entityRepository = entityRepository;
    this.configuration = configuration;
  }

  @Override
  public Mono<Void> create(String metaBundleIdentifier) {
    return Mono.just(metaBundleIdentifier)
        .flatMap(MONO.makeSureAsync(
            metaBundleRepository::notExistsByIdentifier,
            identifier -> new ConflictException("Meta Bundle with identifier '" + identifier + "' already exists!")))
        .flatMap(metaBundleRepository::create);
  }

  @Override
  public Function<String, Mono<Void>> addVersionEntity(String metaBundleIdentifier) {
    return (String versionEntityIdentifier) -> metaBundleRepository.hasVersionEntity(metaBundleIdentifier)
        .flatMap(hasVersionEntity -> hasVersionEntity
            ? entityRepository.addVersion(metaBundleIdentifier, versionEntityIdentifier)
            : entityRepository.addFirstVersion(metaBundleIdentifier, versionEntityIdentifier))
        .delayUntil(entityRepository.makeSpecializationOfGeneralVersion(metaBundleIdentifier))
        .delayUntil(metaBundleRepository.addEntityToMetaBundle(metaBundleIdentifier))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Version has not been added into meta component provenance!")));
  }

  @Override
  public Function<String, Mono<Void>> addToken(String metaBundleIdentifier) {
    return (String jwtToken) -> entityRepository.addToken(metaBundleIdentifier, jwtToken)
        .delayUntil(metaBundleRepository.addEntityToMetaBundle(metaBundleIdentifier))
        .delayUntil(metaBundleRepository.addTokenGenerationToMetaBundle(metaBundleIdentifier))
        .delayUntil(metaBundleRepository.addTokenGeneratorToMetaBundle(metaBundleIdentifier))
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
