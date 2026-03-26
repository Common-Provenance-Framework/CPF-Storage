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

import reactor.core.publisher.Flux;
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
  public Mono<Entity> create(Entity entity) {
    return Mono.justOrEmpty(entity)
        .map(NodeFactory::toEntity)
        .flatMap(MONO.<EntityNode>makeSureNotNullWithMessage("Entity can not be 'null'!"))
        .flatMap(entityRepository::save)
        .flatMap(ProvenanceFactory.entityToProv(configuration))
        .onErrorResume(MONO.exceptionWrapper("EntityPersistence - Error while creating new Entity"));
  }

  @Override
  public Function<Entity, Mono<Entity>> addFirstVersion(Entity general) {

    return (Entity version) -> Mono.zip(
        Mono.justOrEmpty(version).map(NodeFactory::toEntity),
        Mono.justOrEmpty(general).map(NodeFactory::toEntity))
        .flatMap(tuple -> Mono.zip(
            Mono.just(tuple.getT1()),
            Mono.just(tuple.getT2()),
            bundleRepository.findByGeneralEntity(tuple.getT2())))
        .flatMap(tuple -> {
          EntityNode getneralVersion = tuple.getT2();
          EntityNode firstVersion = tuple.getT1().withSpecializationOfEntity(getneralVersion);
          BundleNode metaComponent = tuple.getT3().withEntity(firstVersion);

          return bundleRepository.save(metaComponent).thenReturn(firstVersion);
        })
        .flatMap(ProvenanceFactory.entityToProv(configuration));
  }

  @Override
  public Function<Entity, Mono<Entity>> addNewVersion(Entity general, Entity lastVersion) {
    return (Entity version) -> Mono.zip(
        Mono.justOrEmpty(version).map(NodeFactory::toEntity),
        Mono.justOrEmpty(lastVersion).map(NodeFactory::toEntity),
        Mono.justOrEmpty(general).map(NodeFactory::toEntity))
        .flatMap(tuple -> Mono.zip(
            Mono.just(tuple.getT1()),
            Mono.just(tuple.getT2()),
            Mono.just(tuple.getT3()),
            bundleRepository.findByGeneralEntity(tuple.getT3())))
        .flatMap(tuple -> {
          EntityNode prevVersion = tuple.getT2();
          EntityNode generalVersion = tuple.getT3();
          EntityNode newVersion = tuple.getT1()
              .wihtRevisionOfEntity(prevVersion)
              .withSpecializationOfEntity(generalVersion);
          BundleNode metaComponent = tuple.getT4()
              .withEntity(newVersion);

          return bundleRepository.save(metaComponent).thenReturn(newVersion);
        })
        .flatMap(ProvenanceFactory.entityToProv(configuration));
  }

  @Override
  public Function<Entity, Mono<Entity>> addToken(Entity token, Activity generation, Agent generator) {
    return (Entity version) -> {
      return Mono.justOrEmpty(version)
          .map(NodeFactory::toEntity)
          .flatMap((EntityNode versionEntity) -> Mono.zip(
              Mono.just(versionEntity),
              bundleRepository.findByGeneralEntity(versionEntity),
              Mono.justOrEmpty(token).map(NodeFactory::toEntity),
              Mono.justOrEmpty(generation).map(NodeFactory::toEntity),
              Mono.justOrEmpty(generator).map(NodeFactory::toEntity)))
          .flatMap(tuple -> {
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
                .withAgent(generatorNode)
                .withEntity(tokenNode)
                .withActivity(generationNode);

            return bundleRepository.save(metaComponent).thenReturn(tokenNode);
          })
          .flatMap(ProvenanceFactory.entityToProv(configuration));
    };

  }

  @Override

  public Mono<Entity> getByIdentifier(String identifier) {
    return MONO.<String>makeSureNotNullWithMessage("Entity identifier can not be 'null'!").apply(identifier)
        .flatMap(entityRepository::findByIdentifier)
        .onErrorResume(MONO.exceptionWrapper("EntityPersistence - Error while reading entity"))
        .flatMap(ProvenanceFactory.entityToProv(configuration))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Entity with identifier '" + identifier + "' has not been found!"))));
  }

  @Override
  public Flux<Entity> getAllEntities(String bundleIdentifier) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!").apply(bundleIdentifier)
        .flatMapMany(entityRepository::getAllEntitiesByBundleIdentifier)
        .onErrorResume(MONO.exceptionWrapper("EntityPersistence - Error while reading entities"))
        .flatMap(ProvenanceFactory.entityToProv(configuration));
  }

  @Override
  public Mono<Entity> getGeneralVersionEntity(String bundleIdentifier) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!").apply(bundleIdentifier)
        .flatMap(entityRepository::getGeneralEntityByBundleIdentifier)
        .onErrorResume(MONO.exceptionWrapper("EntityPersistence - Error while reading general entity"))
        .flatMap(ProvenanceFactory.entityToProv(configuration))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException(
                "General version Entity for bundle with identifier '" + bundleIdentifier + "' has not been found!"))));
  }

  @Override
  public Mono<Entity> getLastVersionEntity(String bundleIdentifier) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!").apply(bundleIdentifier)
        .flatMap(entityRepository::getLastVersionEntityByBundleIdentifier)
        .onErrorResume(MONO.exceptionWrapper("EntityPersistence - Error while reading last version entity"))
        .flatMap(ProvenanceFactory.entityToProv(configuration))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException(
                "Last version Entity for bundle with identifier '" + bundleIdentifier + "' has not been found!"))));
  }

  @Override
  public Mono<Integer> getLastVersion(String bundleIdentifier) {
    return MONO.<String>makeSureNotNullWithMessage("Bundle identifier can not be 'null'!").apply(bundleIdentifier)
        .flatMap(entityRepository::getLastVersionByBundleIdentifier)
        .onErrorResume(MONO.exceptionWrapper("EntityPersistence - Error while reading last version entity"))
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException(
                "Last version Entity for bundle with identifier '" + bundleIdentifier + "' has not been found!"))));
  }
}
