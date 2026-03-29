package org.commonprovenance.framework.store.service.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.service.persistence.metaComponent.MetaComponentService;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MetaComponentServiceImpl implements MetaComponentService {

  private final BundlePersistence bundlePersistence;

  public MetaComponentServiceImpl(
      BundlePersistence bundlePersistence) {
    this.bundlePersistence = bundlePersistence;
  }

  @Override
  public Mono<Document> createMetaComponent(QualifiedName metaBundleIdentifier) {
    return this.bundlePersistence.create(metaBundleIdentifier.getLocalPart());
  }

  @Override
  public Function<Document, Mono<Document>> addNewVersion(QualifiedName identifier) {
    return (Document document) -> MONO
        .<Document>makeSureNotNullWithMessage("Meta Component Document can not be 'null'!")
        .apply(document)
        .flatMap(this::getBundleIdentifier)
        .map(QualifiedName::getLocalPart)
        .flatMap((String bundleIdentifier) -> MONO
            .<QualifiedName>makeSureNotNullWithMessage("New version identifier can not be 'null'!")
            .apply(identifier)
            .map(QualifiedName::getLocalPart)
            .flatMap(this.bundlePersistence.createNewVersion(bundleIdentifier)));

  }

  @Override
  public Function<Document, Mono<Document>> addTokenToLastVersion(Token token) {
    return (Document document) -> MONO
        .<Document>makeSureNotNullWithMessage("Meta Component Document can not be 'null'!")
        .apply(document)
        .flatMap(this::getBundleIdentifier)
        .map(QualifiedName::getLocalPart)
        .flatMap((String bundleIdentifier) -> MONO
            .<Token>makeSureNotNullWithMessage("Token can not be 'null'!")
            .apply(token)
            .flatMap(this.bundlePersistence.createToken(bundleIdentifier)));
  }

  @Override
  public Mono<Document> getMetaComponent(QualifiedName metaBundleId) {
    return Mono.justOrEmpty(metaBundleId)
        .flatMap(this::getByIdentifier)
        .onErrorResume(NotFoundException.class,
            _ -> this.createMetaComponent(metaBundleId));
  }

  @Override
  public Mono<Document> getByIdentifier(QualifiedName id) {
    return this.bundlePersistence.getByIdentifier(id.getLocalPart());
  }

  @Override
  public Mono<Boolean> exists(String id) {
    return this.bundlePersistence.exists(id);
  }

  private Mono<QualifiedName> getBundleIdentifier(Document document) {
    return this.getBundle(document)
        .map(Bundle::getId);
  }

  private Mono<Bundle> getBundle(Document document) {
    return Flux.fromIterable(document.getStatementOrBundle())
        .filter(Bundle.class::isInstance)
        .map(Bundle.class::cast)
        .single()
        .onErrorResume(MONO.exceptionWrapper("MetaComponentService - Error while getting Bundle"));

  }

}