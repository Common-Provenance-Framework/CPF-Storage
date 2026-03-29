package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.AgentRepository;
import org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client.AgentNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class AgentNeo4jRepository implements AgentRepository {
  private final AgentNeo4jRepositoryClient client;

  public AgentNeo4jRepository(
      AgentNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<AgentNode> save(AgentNode agent) {
    return client.save(agent);
  }

  @Override
  public Mono<AgentNode> findByIdentifier(String identifier) {
    return client.findByIdentifier(identifier);
  }

}
