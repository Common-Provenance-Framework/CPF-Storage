package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

@Repository
public interface EntityNeo4jRepositoryClient extends ReactiveNeo4jRepository<EntityNode, String> {

  @Query("MATCH (bundle:Bundle {id: $bundleId}) -[r:bundle_entities]->(entity:Entity) RETURN entity")
  Flux<EntityNode> getAllEntitiesByBundleId(@Param("bundleId") String bundleId);
}
