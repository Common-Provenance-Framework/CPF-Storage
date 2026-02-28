package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.function.Function;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.EntityPersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.ProvenanceFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.EntityRepository;
import org.openprovenance.prov.model.Entity;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Component
public class EntityPersistenceImpl implements EntityPersistence {
  private final EntityRepository entityRepository;
  private final BundleRepository bundleRepository;
  private final AppConfiguration configuration;

  public EntityPersistenceImpl(
      EntityRepository entityRepository,
      BundleRepository bundleRepository,
      AppConfiguration configuration) {
    this.entityRepository = entityRepository;
    this.bundleRepository = bundleRepository;
    this.configuration = configuration;
  }

  @Override
  @NotNull
  public Mono<Entity> create(@NotNull Entity entity) {
    return Mono.justOrEmpty(entity)
        .flatMap(NodeFactory::toEntity)
        .flatMap(MONO.<EntityNode>makeSureNotNullWithMessage("Entity can not be 'null'!"))
        .flatMap(entityRepository::save)
        .flatMap(ProvenanceFactory.entityToProv(configuration))
        .onErrorResume(MONO.exceptionWrapper("EntityNeo4jRepository - Error while creating new Entity"));
  }

  @Override
  public @NotNull Function<Entity, Mono<Entity>> addFirstVersion(@NotNull Entity general) {
    return (Entity version) -> Mono.zip(
        Mono.justOrEmpty(version)
            .flatMap(NodeFactory::toEntity),
        Mono.justOrEmpty(general)
            .flatMap(NodeFactory::toEntity))
        .flatMap(tuple -> Mono.zip(
            Mono.just(tuple.getT1()),
            Mono.just(tuple.getT2()),
            bundleRepository.findByGeneralEntity(tuple.getT2())))
        .flatMap(tuple -> {
          EntityNode firstVersion = tuple.getT1();
          EntityNode getneralVersion = tuple.getT2();
          BundleNode metaComponent = tuple.getT3();
          return bundleRepository.save(metaComponent.withNode(firstVersion))
              .then(entityRepository.save(firstVersion.withSpecializationOfEntity(getneralVersion)));
        })
        .flatMap(ProvenanceFactory.entityToProv(configuration));
  }

  @Override
  @NotNull
  public Mono<Entity> getById(@NotNull String id) {
    return MONO.<String>makeSureNotNullWithMessage("Entity Id can not be 'null'!").apply(id)
        .flatMap(entityRepository::findById)
        .onErrorResume(MONO.exceptionWrapper("EntityNeo4jRepository - Error while reading entity"))
        .flatMap(ProvenanceFactory.entityToProv(configuration))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Entity with id '" + id + "' has not been found!"))));
  }

}
