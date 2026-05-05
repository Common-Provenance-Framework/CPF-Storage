package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import java.util.function.Function;

import org.commonprovenance.framework.store.controller.advice.ApplicationExceptionHandler;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.EntityRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.BundleNeo4jRepositoryClient;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.EntityNeo4jRepositoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class EntityNeo4jRepository implements EntityRepository {

  private final Logger LOGGER = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

  private final BundleNeo4jRepositoryClient metaBundleClient;
  private final EntityNeo4jRepositoryClient entityClient;

  public EntityNeo4jRepository(
      BundleNeo4jRepositoryClient metaBundleClient,
      EntityNeo4jRepositoryClient entityClient) {
    this.metaBundleClient = metaBundleClient;
    this.entityClient = entityClient;
  }

  @Override
  public Mono<EntityNode> addFirstVersion(String metaBundleIdentifier, String versionIdentifier) {

    return Mono.just(versionIdentifier)
        .map(fvi -> new EntityNode(fvi, 1))
        .flatMap(entityClient::save)
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("First version entity has not been added into meta component provenance!")));
  }

  @Override
  public Mono<EntityNode> addVersion(String metaBundleIdentifier, String versionIdentifier) {
    return Mono.just(metaBundleIdentifier)
        .flatMap(entityClient::getLastVersion)
        .flatMap(lastVersion -> Mono.just(new EntityNode(versionIdentifier, lastVersion + 1))
            .flatMap(entityClient::save)
            .delayUntil(this.makeRevisionOfVersion(metaBundleIdentifier, lastVersion)))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Version entity has not been added into meta component provenance!")));
  }

  @Override
  public Function<EntityNode, Mono<Void>> makeSpecializationOfGeneralVersion(String metaBundleIdentifier) {
    return entity -> Mono.just(metaBundleIdentifier)
        .flatMap(entityClient::getGeneralVersionIdByMetaBundleIdentifier)
        .flatMap(generalVersionId -> entityClient.createSpecializationOfRelationship(entity.getId(), generalVersionId))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("First and general version entities has not been connected  with 'specializationOf' relation!")))
        .then();
  }

  @Override
  public Function<EntityNode, Mono<Void>> makeRevisionOfVersion(String metaBundleIdentifier, Integer version) {
    return versionEntity -> entityClient.getVersionEntityId(metaBundleIdentifier, version)
        .flatMap(generalEntityId -> entityClient.createRevisionOfRelationship(versionEntity.getId(), generalEntityId))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("New and last version entities has not been connected  with 'revisionOf' relation!")))
        .then();
  }

  // TODO: to BundleNeo4jRepository
  @Override
  public Function<EntityNode, Mono<Void>> addToBundle(String metaBundleIdentifier) {
    // TODO: add test and handler
    return entity -> Mono.just(metaBundleIdentifier)
        .flatMap(metaBundleClient::getIdByIdentifier)
        .flatMap(metaBundleId -> metaBundleClient.createBundleEntitiesRelationship(metaBundleId, entity.getId()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("First version entity has not been connected into metaBundle!")))
        .then();
  }

}
