package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.function.Function;

import org.commonprovenance.framework.store.common.utils.JwtUtils;
import org.commonprovenance.framework.store.controller.advice.ApplicationExceptionHandler;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.JwtTokenToNodeFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasGeneratedBy;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.EntityRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.ActivityNeo4jRepositoryClient;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.MetaBundleNeo4jClient;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.EntityNeo4jRepositoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class EntityNeo4jRepository implements EntityRepository {

  private final Logger LOGGER = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

  private final MetaBundleNeo4jClient metaBundleClient;
  private final EntityNeo4jRepositoryClient entityClient;
  private final ActivityNeo4jRepositoryClient activityClient;

  public EntityNeo4jRepository(
      MetaBundleNeo4jClient metaBundleClient,
      EntityNeo4jRepositoryClient entityClient,
      ActivityNeo4jRepositoryClient activityClient) {
    this.metaBundleClient = metaBundleClient;
    this.entityClient = entityClient;
    this.activityClient = activityClient;
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

  public Mono<EntityNode> addToken(String metaBundleIdentifier, String jwtToken) {
    return MONO.fromEither(JwtUtils.extractBundleIdentifier(jwtToken))
        .flatMap(entityClient::getIdByIdentifier)
        .flatMap(versionId -> MONO.fromEither(JwtTokenToNodeFactory.toTokenEntity(jwtToken))
            .flatMap(entityClient::save)
            .delayUntil(this.addTokenToBundle(versionId))
            .delayUntil(this.addGenerationToBundle(versionId)))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token has not been added into meta componenet provenance!")));
  }

  private Function<EntityNode, Mono<Void>> addTokenToBundle(String versionId) {
    return tokenNode -> entityClient.createWasDerivedFromRelationship(tokenNode.getId(), versionId)
        .then();
  }

  private Function<EntityNode, Mono<Void>> addGenerationToBundle(String versionId) {
    return tokenNode -> Mono.just(tokenNode)
        .map(EntityNode::getWasGeneratedBy)
        .flatMapMany(Flux::fromIterable)
        .map(WasGeneratedBy::getActivity)
        .map(ActivityNode::getId)
        .flatMap(id -> activityClient.createUsedRelationship(id, versionId))
        .then();

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

}
