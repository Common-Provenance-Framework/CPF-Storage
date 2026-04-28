package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.BundleNeo4jRepositoryClient;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.EntityNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import io.vavr.Function2;
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
  public Mono<BundleNode> create(String identifier) {
    return Mono.just(identifier)
        .flatMap(MONO::makeSureNotNull)
        .flatMap(MONO.makeSureAsync(
            this::notExistsByIdentifier,
            id -> new ConflictException("Bundle with identifier '" + id + "' already exists!")))
        .map(BundleNode::new)
        .map(BundleNode::withGeneralEntity)
        .flatMap(this::save);
  }

  @Override
  public Function<String, Mono<Void>> addVersionEntity(String identifier) {
    return (String versionEntityIdentifier) -> bundleClient.hasVersionEntity(identifier)
        .flatMap(hasLastVersion -> hasLastVersion
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

  public Function<String, Mono<Boolean>> addEntityIntoBundle(String identifier) {
    return (String entityIdentifier) -> bundleClient.createBundleEntitiesRelationship(identifier, entityIdentifier);
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
  public Mono<BundleNode> save(BundleNode bundle) {
    return bundleClient.save(bundle);
  }
  // --- old API

  @Override
  public Mono<BundleNode> findByIdentifier(String identifier) {
    return bundleClient.getIdByIdentifier(identifier)
        .flatMap(bundleClient::findById);
  }

  @Override
  public Mono<BundleNode> findByGeneralEntity(EntityNode entity) {
    return bundleClient.getBundleByGeneralEntity(entity.getIdentifier());
  }

  @Override
  public Mono<Boolean> hasVersionEntity(String identifier) {
    return bundleClient.hasVersionEntity(identifier);
  }

  @Override
  public Mono<Boolean> hasNotVersionEntity(String identifier) {
    return bundleClient.hasVersionEntity(identifier).map(v -> !v);
  }
}
