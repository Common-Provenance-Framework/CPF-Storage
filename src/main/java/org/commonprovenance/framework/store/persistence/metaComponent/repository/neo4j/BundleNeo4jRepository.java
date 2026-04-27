package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.BundleRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.BundleNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class BundleNeo4jRepository implements BundleRepository {
  private final BundleNeo4jRepositoryClient client;

  public BundleNeo4jRepository(
      BundleNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<BundleNode> create(String identifier) {
    return Mono.just(identifier)
        .flatMap(MONO::makeSureNotNull)
        .flatMap(MONO.makeSureAsync(
            this::notExists,
            id -> new ConflictException("Bundle with identifier '" + id + "' already exists!")))
        .map(BundleNode::new)
        .flatMap(client::save);
  }

  @Override
  public Mono<Boolean> exists(String identifier) {
    return client.existsByIdentifier(identifier);
  }

  @Override
  public Mono<Boolean> notExists(String identifier) {
    return this.exists(identifier)
        .map(exists -> !exists);
  }

  // --- old API

  @Override
  public Mono<BundleNode> save(BundleNode bundle) {
    return client.save(bundle);
  }

  @Override
  public Mono<BundleNode> findByIdentifier(String identifier) {
    return client.getIdByIdentifier(identifier)
        .flatMap(client::findById);
  }

  @Override
  public Mono<BundleNode> findByGeneralEntity(EntityNode entity) {
    return client.getBundleByGeneralEntity(entity.getIdentifier());
  }

  @Override
  public Mono<Boolean> hasVersionEntity(String identifier) {
    return client.hasVersionEntity(identifier);
  }

  @Override
  public Mono<Boolean> hasNotVersionEntity(String identifier) {
    return client.hasVersionEntity(identifier).map(v -> !v);
  }
}
