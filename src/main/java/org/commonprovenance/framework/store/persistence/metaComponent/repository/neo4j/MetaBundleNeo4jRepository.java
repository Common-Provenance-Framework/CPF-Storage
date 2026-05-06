package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasAttributedTo;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasGeneratedBy;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.MetaBundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.MetaBundleNeo4jClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class MetaBundleNeo4jRepository implements MetaBundleRepository {
  private final String LOG_PREFIX = "MetaBundleNeo4jRepository: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(MetaBundleNeo4jRepository.class);

  private final MetaBundleNeo4jClient metaBundleClient;

  public MetaBundleNeo4jRepository(
      MetaBundleNeo4jClient metaBundleClient) {
    this.metaBundleClient = metaBundleClient;
  }

  @Override
  public Mono<Void> create(String metaBundleIdentifier) {
    return Mono.just(metaBundleIdentifier)
        .map(BundleNode::new)
        .map(BundleNode::withGeneralEntity)
        .flatMap(metaBundleClient::save)
        .then()
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Meta provenance component with general version entity has been created."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Meta provenance component has not been created!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Meta provenance component has not been created!")));
  }

  @Override
  public Mono<Boolean> hasVersionEntity(String metaBundleIdentifier) {
    return Mono.just(metaBundleIdentifier)
        .flatMap(metaBundleClient::hasVersionEntity)
        .doOnSuccess(hasVersion -> {
          if (hasVersion)
            LOGGER.trace("Meta provenance component has version entity.");
          else
            LOGGER.trace("Meta provenance component has no version entity.");
        })
        .doOnError(throwable -> LOGGER.error("Error while check bundle version existence!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Error while check bundle version existence!")));
  }

  @Override
  public Mono<Boolean> existsByIdentifier(String metaBundleIdentifier) {
    return metaBundleClient.existsByIdentifier(metaBundleIdentifier)
        .doOnSuccess(exists -> {
          if (exists)
            LOGGER.trace(LOG_PREFIX + "Meta provenance component with identifier '" + metaBundleIdentifier + "' exists.");
          else
            LOGGER.trace(LOG_PREFIX + "Meta provenance component with identifier '" + metaBundleIdentifier + "' does not exists.");
        })
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Error while check meta provenance component existence!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Error while check meta provenance component existence!")));
  }

  @Override
  public Mono<Boolean> notExistsByIdentifier(String metaBundleIdentifier) {
    return this.existsByIdentifier(metaBundleIdentifier)
        .map(exists -> !exists);
  }

  @Override
  public Mono<BundleNode> findByIdentifier(String metaBundleIdentifier) {
    return Mono.just(metaBundleIdentifier)
        .flatMap(metaBundleClient::getIdByIdentifier)
        .flatMap(metaBundleClient::findById)
        .switchIfEmpty(Mono.error(() -> new NotFoundException("Bundle with identifier '" + metaBundleIdentifier + "' has not been found!")))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Meta provenance component with identifier '" + metaBundleIdentifier + "' has been found."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException _)
            LOGGER.warn(LOG_PREFIX + "Meta provenance component with identifier '" + metaBundleIdentifier + "' has not been found.");
          else
            LOGGER.error(LOG_PREFIX + "Error while searching meta provenance component with identifier '" + metaBundleIdentifier + "'!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Error while searching meta provenance component with identifier '" + metaBundleIdentifier + "'!")));
  }

  @Override
  public Function<EntityNode, Mono<Void>> addEntityToMetaBundle(String metaBundleIdentifier) {
    return (EntityNode entity) -> metaBundleClient.createBundleEntitiesRelationship(metaBundleIdentifier, entity.getId())
        .switchIfEmpty(Mono.error(() -> new InternalApplicationException("Entity has not been connected to Bundle!")))
        .then()
        .doOnSuccess(_ -> LOGGER.trace(
            LOG_PREFIX + "Entity with identifier '" + entity.getIdentifier() + "' has been connected to Bundle '" + metaBundleIdentifier + "'."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Entity with identifier '" + entity.getIdentifier() + "' has not been connected to Bundle '" + metaBundleIdentifier + "'.\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Entity has not been connected to Bundle!")));
  }

  @Override
  public Function<EntityNode, Mono<Void>> addTokenGenerationToMetaBundle(String metaBundleIdentifier) {
    return (EntityNode tokenNode) -> Mono.just(tokenNode)
        .map(EntityNode::getWasGeneratedBy)
        .flatMapMany(Flux::fromIterable)
        .map(WasGeneratedBy::getActivity)
        .map(ActivityNode::getId)
        .single()
        .flatMap(generationId -> metaBundleClient.createBundleActivitiesRelationship(metaBundleIdentifier, generationId))
        .switchIfEmpty(Mono.error(() -> new InternalApplicationException("Generation activity has not been connected to Bundle!")))
        .then()
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Generation activity has been connected to Bundle '" + metaBundleIdentifier + "'."))
        .doOnError(
            throwable -> LOGGER.error(LOG_PREFIX + "Generation activity has not been connected to Bundle '" + metaBundleIdentifier + "'.\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token Generation has not been connected to Bundle!")));
  }

  @Override
  public Function<EntityNode, Mono<Void>> addTokenGeneratorToMetaBundle(String metaBundleIdentifier) {
    return tokenNode -> Mono.just(tokenNode)
        .map(EntityNode::getWasAttributedTo)
        .flatMapMany(Flux::fromIterable)
        .map(WasAttributedTo::getAgent)
        .map(AgentNode::getId)
        .single()
        .flatMap(generatorId -> metaBundleClient.createBundleAgentsRelationship(metaBundleIdentifier, generatorId))
        .switchIfEmpty(Mono.error(() -> new InternalApplicationException("Generator agent has not been connected to Bundle!")))
        .then()
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Generator agent has been connected to Bundle '" + metaBundleIdentifier + "'."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Generator agent has not been connected to Bundle '" + metaBundleIdentifier + "'.\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token Generator agent has not been connected to Bundle!")));
  }

}
