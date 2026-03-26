package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface EntityNeo4jRepositoryClient extends ReactiveNeo4jRepository<EntityNode, String> {

  @Query("""
          MATCH (entity:Entity {identifier: $identifier})
          RETURN entity
      """)
  Mono<EntityNode> findByIdentifier(@Param("identifier") String identifier);

  @Query("""
          MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[r:bundle_entities]->(entity:Entity)
          WHERE entity.`pav:version` IS NULL
          RETURN entity
      """)
  Mono<EntityNode> findGeneralVersion(@Param("bundleIdentifier") String identifier);

  @Query("""
          MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[r:bundle_entities]->(entity:Entity)
          WHERE entity.`pav:version` IS NOT NULL
          RETURN entity
          ORDER BY toInteger(entity.`pav:version`) DESC
          LIMIT 1
      """)
  Mono<EntityNode> findLastVersion(@Param("bundleIdentifier") String identifier);

  @Query("""
          MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[r:bundle_entities]->(entity:Entity)
          RETURN entity
      """)
  Flux<EntityNode> getAllEntitiesByBundleIdentifier(@Param("bundleIdentifier") String bundleIdentifier);
}
