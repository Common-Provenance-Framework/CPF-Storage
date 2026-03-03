package org.commonprovenance.framework.store.persistence.metaComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.function.Function;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.EntityPersistence;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.ProvenanceFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.EntityRepository;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Entity;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple5;

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
          return bundleRepository.save(metaComponent.withNode(firstVersion)) // TODO: ??Maybe save together??
              .then(entityRepository.save(firstVersion.withSpecializationOfEntity(getneralVersion)));
        })
        .flatMap(ProvenanceFactory.entityToProv(configuration));
  }

  @Override
  public @NotNull Function<Entity, Mono<Entity>> addToken(
      @NotNull Entity token,
      @NotNull Activity generation,
      @NotNull Agent generator) {

    return (Entity version) -> {
      return NodeFactory.toEntity(version)
          .flatMap((EntityNode versionEntity) -> Mono.zip(
              Mono.just(versionEntity),
              bundleRepository.findByGeneralEntity(versionEntity),
              Mono.justOrEmpty(token).flatMap(NodeFactory::toEntity),
              Mono.justOrEmpty(generation).flatMap(NodeFactory::toEntity),
              Mono.justOrEmpty(generator).flatMap(NodeFactory::toEntity)))

          .delayUntil(tuple -> {
            AgentNode generatorNode = tuple.getT5();
            EntityNode versionEntityNode = tuple.getT1();

            ActivityNode generationNode = tuple.getT4()
                .withUsedEntity(versionEntityNode)
                .withWasAssociatedWithAgent(generatorNode);

            EntityNode tokenNode = tuple.getT3()
                .withWasDerivedFromEntity(versionEntityNode)
                .withWasGeneratedByActivity(generationNode)
                .withWasAttributedToAgent(generatorNode);

            BundleNode metaComponent = tuple.getT2()
                .withNode(generatorNode)
                .withNode(tokenNode)
                .withNode(generationNode);

            return bundleRepository.save(metaComponent);
          })
          .map(Tuple5<EntityNode, BundleNode, EntityNode, ActivityNode, AgentNode>::getT3)
          .flatMap(ProvenanceFactory.entityToProv(configuration));
    };

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

  @Override
  public @NotNull Flux<Entity> getAllEntities(@NotNull String bundleId) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle Id can not be 'null'!").apply(bundleId)
        .flatMapMany(entityRepository::getAllEntitiesByBundleId)
        .onErrorResume(MONO.exceptionWrapper("EntityNeo4jRepository - Error while reading entity"))
        .flatMap(ProvenanceFactory.entityToProv(configuration));
  }
}
