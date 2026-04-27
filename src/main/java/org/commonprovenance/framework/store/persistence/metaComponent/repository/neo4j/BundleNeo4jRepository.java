package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

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
  public Mono<Boolean> exists(String identifier) {
    return client.getIdByIdentifier(identifier)
        .flatMap(client::existsById);
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
