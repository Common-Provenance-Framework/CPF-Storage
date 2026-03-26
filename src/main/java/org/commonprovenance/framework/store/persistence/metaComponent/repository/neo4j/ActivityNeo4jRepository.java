package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.ActivityRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.ActivityNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class ActivityNeo4jRepository implements ActivityRepository {
  private final ActivityNeo4jRepositoryClient client;

  public ActivityNeo4jRepository(
      ActivityNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<ActivityNode> save(ActivityNode activity) {
    return client.save(activity);
  }

  @Override
  public Mono<ActivityNode> findByIdentifier(String identifier) {
    return client.findByIdentifier(identifier);
  }

}
