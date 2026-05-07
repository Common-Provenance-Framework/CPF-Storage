package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.function.Function;

import org.commonprovenance.framework.store.common.utils.JwtUtils;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.JwtTokenToNodeFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasGeneratedBy;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.EntityRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.ActivityNeo4jRepositoryClient;
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
  private final String LOG_PREFIX = "EntityNeo4jRepository: ";
  private final Logger LOGGER = LoggerFactory.getLogger(EntityNeo4jRepository.class);

  private final EntityNeo4jRepositoryClient entityClient;
  private final ActivityNeo4jRepositoryClient activityClient;

  public EntityNeo4jRepository(
      EntityNeo4jRepositoryClient entityClient,
      ActivityNeo4jRepositoryClient activityClient) {
    this.entityClient = entityClient;
    this.activityClient = activityClient;
  }

  @Override
  public Function<Integer, Mono<EntityNode>> createBundleVersionEntity(String metaBundleIdentifier, String versionEntityIdentifier) {
    return (Integer versionNo) -> Mono.just(new EntityNode(versionEntityIdentifier, versionNo))
        .flatMap(entityClient::save)
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Bundle version entity has been added into meta component provenance."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Bundle version entity has not been added into meta component provenance!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Bundle version entity has not been added into meta component provenance!")));
  }

  @Override
  public Mono<EntityNode> createBundleTokenEntity(String metaBundleIdentifier, String jwtToken) {
    return Mono.just(jwtToken)
        .map(JwtTokenToNodeFactory::toTokenEntity)
        .flatMap(MONO::fromEither)
        .flatMap(entityClient::save)
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token entity has been added into meta component provenance."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Token entity has not been added into meta component provenance!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token entity has not been added into meta component provenance!")));
  }

  @Override
  public Function<EntityNode, Mono<Void>> addToBundleVersionEntity(String jwtToken) {
    return (EntityNode tokenNode) -> Mono.just(jwtToken)
        .map(JwtUtils::extractBundleIdentifier)
        .flatMap(MONO::fromEither)
        .flatMap(entityClient::getIdByIdentifier)
        .flatMap(bundleVersionId -> Mono.when(
            Mono.just(tokenNode)
                .map(EntityNode::getId)
                .flatMap(id -> entityClient.createWasDerivedFromRelationship(id, bundleVersionId))
                .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token entity has been connected with bundle version with 'wasDerivedFrom' relation."))
                .doOnError(throwable -> LOGGER.error(
                    LOG_PREFIX + "Token entity has not been connected with bundle version with 'wasDerivedFrom' relation!\n" + throwable.getMessage())),
            Mono.just(tokenNode)
                .map(EntityNode::getWasGeneratedBy)
                .flatMapMany(Flux::fromIterable)
                .single()
                .map(WasGeneratedBy::getActivity)
                .map(ActivityNode::getId)
                .flatMap(id -> activityClient.createUsedRelationship(id, bundleVersionId))
                .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token generation activity has been connected with bundle version with 'used' relation."))
                .doOnError(throwable -> LOGGER.error(
                    LOG_PREFIX + "Token generation activity has not been connected with bundle version with 'used' relation!\n" + throwable.getMessage()))))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token Entity has been connected with bundle version entity."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Token Entity has not been connected with bundle version entity!\n" + throwable.getMessage()))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token Entity has not been connected with bundle version entity!")));

  }

  @Override
  public Function<EntityNode, Mono<Void>> makeSpecializationOfGeneralVersion(String metaBundleIdentifier) {
    return entity -> Mono.just(metaBundleIdentifier)
        .flatMap(entityClient::getGeneralVersionIdByMetaBundleIdentifier)
        .flatMap(generalVersionId -> entityClient.createSpecializationOfRelationship(entity.getId(), generalVersionId))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Bundle version entity has been connected with general version entity with 'specializationOf' relation."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Bundle version entity has not been connected with general version entity with 'specializationOf' relation!\n" + throwable.getMessage()))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException(
            "Bundle version entity has not been connected with general version entity with 'specializationOf' relation!")));
  }

  @Override
  public Function<EntityNode, Mono<Void>> makeRevisionOfPreviousVersion(String metaBundleIdentifier) {
    return (EntityNode versionEntity) -> Mono.just(versionEntity)
        .map(EntityNode::getPav)
        .map(pav -> pav.get("version"))
        .map(Integer.class::cast)
        .flatMap(version -> version == 1
            ? Mono.empty()
            : Mono.just(version - 1))
        .flatMap(prevVersion -> entityClient.getVersionEntityId(metaBundleIdentifier, prevVersion))
        .flatMap(prevVersionId -> entityClient.createRevisionOfRelationship(versionEntity.getId(), prevVersionId))
        .doOnSuccess(connected -> {
          if (connected == null)
            LOGGER.trace(LOG_PREFIX + "This is first bundle version. There is no previous version to connect with.");
          else
            LOGGER.trace(LOG_PREFIX + "New and last bundle version entities has been connected  with 'revisionOf' relation.");
        })
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "New and last bundle version entities has not been connected  with 'revisionOf' relation!\n" + throwable.getMessage()))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("New and last bundle version entities has not been connected  with 'revisionOf' relation!")));
  }

}
