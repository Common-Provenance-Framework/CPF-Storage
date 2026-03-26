package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface ActivityNeo4jRepositoryClient extends ReactiveNeo4jRepository<ActivityNode, String> {
  @Query("""
          MATCH (activity:Activity {identifier: $identifier})
          RETURN activity
      """)
  Mono<ActivityNode> findByIdentifier(@Param("identifier") String identifier);

}
