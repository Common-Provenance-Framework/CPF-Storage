package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.EntityRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.EntityNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class EntityNeo4jRepository implements EntityRepository {
  private final EntityNeo4jRepositoryClient client;

  public EntityNeo4jRepository(
      EntityNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<EntityNode> save(EntityNode entity) {
    return client.save(entity);
  }

  @Override
  public Mono<EntityNode> findById(String id) {
    return client.findById(id);
  }

  @Override
  public Flux<EntityNode> getAllEntitiesByBundleId(String bundleId) {
    return client.getAllEntitiesByBundleId(bundleId);
  }

}
