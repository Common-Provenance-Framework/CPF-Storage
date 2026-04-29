package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;
import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.commonprovenance.framework.store.common.utils.JwtUtils;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.BundleNeo4jRepositoryClient;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.EntityNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.vavr.Function2;
import io.vavr.control.Either;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class BundleNeo4jRepository implements BundleRepository {
  private final BundleNeo4jRepositoryClient bundleClient;
  private final EntityNeo4jRepositoryClient entityClient;

  public BundleNeo4jRepository(
      BundleNeo4jRepositoryClient bundleClient,
      EntityNeo4jRepositoryClient entityClient) {
    this.bundleClient = bundleClient;
    this.entityClient = entityClient;
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
        .then();
  }

  @Override
  public Function<String, Mono<Void>> addVersionEntity(String identifier) {
    return (String versionEntityIdentifier) -> bundleClient.hasVersionEntity(identifier)
        .flatMap(hasVersionEntity -> hasVersionEntity
            ? MONO.combineM(
                entityClient.findGeneralVersion(identifier),
                entityClient.findLastVersion(identifier),
                this.addNextVersion(identifier, versionEntityIdentifier))
            : entityClient.findGeneralVersion(identifier)
                .flatMap(addFirstVersion(identifier, versionEntityIdentifier)));
  }

  private Function<EntityNode, Mono<Void>> addFirstVersion(
      String bundleIdentifier,
      String versionIdentifier) {
    return (EntityNode generalVersion) -> Mono.just(versionIdentifier)
        .map(fvi -> new EntityNode(fvi, 1)
            .withSpecializationOfEntity(generalVersion))
        .delayUntil(entityClient::save)
        .map(EntityNode::getIdentifier)
        .delayUntil(id -> bundleClient.createBundleEntitiesRelationship(bundleIdentifier, id))
        .then();
  }

  private Function2<EntityNode, EntityNode, Mono<Void>> addNextVersion(
      String bundleIdentifier,
      String versionIdentifier) {
    return (EntityNode generalVersion, EntityNode lastVersion) -> Mono.just(versionIdentifier)
        .flatMap(nve -> entityClient.getLastVersion(bundleIdentifier)
            .map(version -> new EntityNode(versionIdentifier, version + 1)
                .withSpecializationOfEntity(generalVersion)
                .withRevisionOfEntity(lastVersion)))
        .delayUntil(entityClient::save)
        .map(EntityNode::getIdentifier)
        .delayUntil(id -> bundleClient.createBundleEntitiesRelationship(bundleIdentifier, id))
        .then();
  }

  public Function<String, Mono<Void>> addToken(String identifier) {
    return (String jwtToken) -> {
      return MONO.fromEither(JwtUtils.extractBundleIdentifier(jwtToken))
          .flatMap(entityClient::findByIdentifier)
          .flatMap(versionEntity -> {
            // TODO: Create factory method for this
            Either<ApplicationException, AgentNode> tokenGeneratorNodeOrException = EITHER.<Map<String, Object>, String, AgentNode> combine(
                Either.<ApplicationException, String> right(jwtToken)
                    .flatMap(JwtUtils::extractTokenGeneratorAttributes),
                Either.<ApplicationException, String> right(jwtToken)
                    .flatMap(JwtUtils::extractTokenGeneratorIdentifier),
                (cpmAttrs, authorityId) -> new AgentNode(
                    authorityId,
                    // TODO: Create Enum for konown types
                    "cpm:TrustedParty",
                    cpmAttrs));

            // TODO: Create factory method for this
            Either<ApplicationException, ActivityNode> tokenGenerationNodeOrException = EITHER.<AgentNode, String, ActivityNode> combine(
                tokenGeneratorNodeOrException,
                JwtUtils.extractTokenCreationString(jwtToken),
                (generator, createdOn) -> new ActivityNode(
                    UUID.randomUUID().toString(),
                    // TODO: Create Enum for konown types
                    "cpm:TokenGeneration",
                    createdOn,
                    createdOn)
                    .withUsedEntity(versionEntity)
                    .withWasAssociatedWithAgent(generator));

            // TODO: Create factory method for this
            Either<ApplicationException, EntityNode> tokenNodeOrException = EITHER
                .<AgentNode, ActivityNode, EntityNode> combine(
                    tokenGeneratorNodeOrException,
                    tokenGenerationNodeOrException,
                    (tokenGeneratorNode, tokenGenerationNode) -> new EntityNode(
                        UUID.randomUUID().toString(),
                        // TODO: Create Enum for konown types
                        "cpm:Token",
                        Map.of("jwt", jwtToken))
                        .withWasDerivedFromEntity(versionEntity)
                        .withWasGeneratedByActivity(tokenGenerationNode)
                        .withWasAttributedToAgent(tokenGeneratorNode));

            return MONO.fromEither(tokenNodeOrException)
                .flatMap(entityClient::save)
                .thenEmpty(MONO.fromEither(tokenNodeOrException)
                    .map(EntityNode::getIdentifier)
                    .flatMap(entityIdentifier -> bundleClient.createBundleEntitiesRelationship(identifier, entityIdentifier))
                    .then())
                .thenEmpty(MONO.fromEither(tokenGenerationNodeOrException)
                    .map(ActivityNode::getIdentifier)
                    .flatMap(activityIdentifier -> bundleClient.createBundleActivitiesRelationship(identifier, activityIdentifier))
                    .then())
                .thenEmpty(MONO.fromEither(tokenGeneratorNodeOrException)
                    .map(AgentNode::getIdentifier)
                    .flatMap(activityIdentifier -> bundleClient.createBundleActivitiesRelationship(identifier, activityIdentifier))
                    .then());
          });

    };
  }

  @Override
  public Mono<Boolean> existsByIdentifier(String identifier) {
    return bundleClient.existsByIdentifier(identifier);
  }

  @Override
  public Mono<Boolean> notExistsByIdentifier(String identifier) {
    return this.existsByIdentifier(identifier)
        .map(exists -> !exists);
  }

  @Override
  public Mono<BundleNode> findByIdentifier(String identifier) {
    return bundleClient.getIdByIdentifier(identifier)
        .flatMap(bundleClient::findById)
        .switchIfEmpty(Mono.defer(() -> Mono
            .error(new NotFoundException("Bundle with identifier '" + identifier + "' has not been found!"))));
  }

}
