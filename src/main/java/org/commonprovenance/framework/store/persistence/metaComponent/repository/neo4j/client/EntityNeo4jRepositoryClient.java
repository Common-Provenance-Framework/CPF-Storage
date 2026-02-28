package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface EntityNeo4jRepositoryClient extends ReactiveNeo4jRepository<EntityNode, String> {

  @Query("MATCH (b:Bundle) -[r:contains]->(g:Entity {id: $generalEntityId}) RETURN b.id")
  Mono<String> getBundleIdByGenenralEntity(String generalEntityId);
}
