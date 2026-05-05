package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.function.Function;

import org.commonprovenance.framework.store.common.utils.JwtUtils;
import org.commonprovenance.framework.store.controller.advice.ApplicationExceptionHandler;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.factory.JwtTokenToNodeFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasAttributedTo;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasGeneratedBy;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.ActivityNeo4jRepositoryClient;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.BundleNeo4jRepositoryClient;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.EntityNeo4jRepositoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class BundleNeo4jRepository implements BundleRepository {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

  private final BundleNeo4jRepositoryClient bundleClient;
  private final EntityNeo4jRepositoryClient entityClient;
  private final ActivityNeo4jRepositoryClient activityClient;

  public BundleNeo4jRepository(
      BundleNeo4jRepositoryClient bundleClient,
      EntityNeo4jRepositoryClient entityClient,
      ActivityNeo4jRepositoryClient activityClient) {
    this.bundleClient = bundleClient;
    this.entityClient = entityClient;
    this.activityClient = activityClient;
  }

  @Override
  public Mono<Void> create(String identifier) {
    return Mono.just(identifier)
        .flatMap(MONO.makeSureAsync(
            this::notExistsByIdentifier,
            bundleIdentifier -> new ConflictException("Bundle with identifier '" + bundleIdentifier + "' already exists!")))
        .map(BundleNode::new)
        .map(BundleNode::withGeneralEntity)
        .flatMap(bundleClient::save)
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Meta component provenance has not been created!")))
        .then();
  }

  public Mono<EntityNode> addToken(String metaBundleIdentifier, String jwtToken) {
    return MONO.fromEither(JwtUtils.extractBundleIdentifier(jwtToken))
        .flatMap(entityClient::getIdByIdentifier)
        .flatMap(versionId -> MONO.fromEither(JwtTokenToNodeFactory.toTokenEntity(jwtToken))
            .flatMap(entityClient::save)
            .delayUntil(this.addTokenToBundle(versionId))
            .delayUntil(this.addGenerationToBundle(versionId))
        // .delayUntil(this.addTokenToMetaBundle(identifier))
        // .delayUntil(this.addTokenGenerationToBundle(identifier))
        // .delayUntil(this.addTokenGeneratorToBundle(identifier))
        // .then()
        )
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

  public Function<EntityNode, Mono<Void>> addTokenToMetaBundle(String identifier) {
    return tokenNode -> Mono.just(identifier)
        .flatMap(bundleClient::getIdByIdentifier)
        .delayUntil(metaBundleId -> bundleClient.createBundleEntitiesRelationship(metaBundleId, tokenNode.getId()))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token has not been connected to Bundle!")));
  }

  public Function<EntityNode, Mono<Void>> addTokenGenerationToBundle(String identifier) {
    return tokenNode -> Mono.just(tokenNode)
        .map(EntityNode::getWasGeneratedBy)
        .flatMapMany(Flux::fromIterable)
        .map(WasGeneratedBy::getActivity)
        .map(ActivityNode::getId)
        .flatMap(generationId -> bundleClient.createBundleActivitiesRelationship(identifier, generationId))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token Generation has not been connected to Bundle!")));
  }

  public Function<EntityNode, Mono<Void>> addTokenGeneratorToBundle(String identifier) {
    return tokenNode -> Mono.just(tokenNode)
        .map(EntityNode::getWasAttributedTo)
        .flatMapMany(Flux::fromIterable)
        .map(WasAttributedTo::getAgent)
        .map(AgentNode::getId)
        .flatMap(generatorId -> bundleClient.createBundleAgentsRelationship(identifier, generatorId))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token Generator has not been connected to Bundle!")));
  }

  @Override
  public Mono<Boolean> existsByIdentifier(String identifier) {
    return bundleClient.existsByIdentifier(identifier)
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException()));
  }

  @Override
  public Mono<Boolean> notExistsByIdentifier(String identifier) {
    return this.existsByIdentifier(identifier)
        .map(exists -> !exists)
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException()));
  }

  @Override
  public Mono<BundleNode> findByIdentifier(String identifier) {
    return bundleClient.getIdByIdentifier(identifier)
        .flatMap(bundleClient::findById)
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Bundle with identifier '" + identifier + "' has not been found!"))))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException()));

  }

  @Override
  public Mono<Boolean> hasVersionEntity(String identifier) {
    return bundleClient.hasVersionEntity(identifier);
  }

}
