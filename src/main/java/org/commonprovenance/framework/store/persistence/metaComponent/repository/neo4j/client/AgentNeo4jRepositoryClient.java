package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface AgentNeo4jRepositoryClient extends ReactiveNeo4jRepository<AgentNode, String> {
  @Query("""
          MATCH (agent:Agent {identifier: $identifier})
          RETURN agent
      """)
  Mono<AgentNode> findByIdentifier(@Param("identifier") String identifier);

}
