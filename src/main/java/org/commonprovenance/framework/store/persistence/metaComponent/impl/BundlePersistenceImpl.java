package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.BundlePersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.openprovenance.prov.model.Document;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Component
public class BundlePersistenceImpl implements BundlePersistence {

  private final BundleRepository repository;

  public BundlePersistenceImpl(
      BundleRepository repository) {
    this.repository = repository;
  }

  @Override
  @NotNull
  public Mono<BundleNode> create(@NotNull Document bundle) {
    return MONO.<Document>makeSureNotNullWithMessage("Bundle can not be 'null'!").apply(bundle)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("BundleNeo4jRepository - Error while creating new Bundle"));
  }

  @Override
  @NotNull
  public Mono<BundleNode> getById(@NotNull UUID uuid) {
    return MONO.<UUID>makeSureNotNullWithMessage("Bundle Id can not be 'null'!").apply(uuid)
        .map(UUID::toString)
        .flatMap(repository::findById)
        .onErrorResume(MONO.exceptionWrapper("BundleNeo4jRepository - Error while reading bundle"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Bundle with id '" + uuid.toString() + "' has not been found!"))));
  }

}
