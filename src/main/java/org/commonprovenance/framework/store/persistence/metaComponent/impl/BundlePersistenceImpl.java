package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import java.util.function.Function;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.ProvenanceFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.openprovenance.prov.model.Document;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class BundlePersistenceImpl implements BundlePersistence {

  private final BundleRepository bundleRepository;
  private final AppConfiguration configuration;

  public BundlePersistenceImpl(
      BundleRepository bundleRepository,
      AppConfiguration configuration) {
    this.bundleRepository = bundleRepository;
    this.configuration = configuration;
  }

  @Override
  public Mono<Void> create(String identifier) {
    return Mono.just(identifier)
        .flatMap(bundleRepository::create);
  }

  @Override
  public Function<String, Mono<Void>> addVersionEntity(String identifier) {
    return (String versionIdentifier) -> Mono.just(versionIdentifier)
        .flatMap(bundleRepository.addVersionEntity(identifier));
  }

  @Override
  public Function<String, Mono<Void>> addToken(String identifier) {
    return (String jwtToken) -> Mono.just(jwtToken)
        .flatMap(bundleRepository.addToken(identifier));
  }

  @Override
  public Mono<Document> getByIdentifier(String identifier) {
    return Mono.just(identifier)
        .flatMap(bundleRepository::findByIdentifier)
        .flatMap(ProvenanceFactory.bundleToProv(configuration));
  }

  @Override
  public Mono<Boolean> exists(String identifier) {
    return Mono.just(identifier)
        .flatMap(bundleRepository::existsByIdentifier);
  }

  @Override
  public Mono<Boolean> notExists(String identifier) {
    return this.exists(identifier).map(exists -> !exists);
  }

}
