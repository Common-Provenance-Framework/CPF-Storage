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
        MATCH (entity:Entity)
        WHERE entity.identifier = $identifier
        RETURN entity
      """)
  Mono<EntityNode> findByIdentifier(@Param("identifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"] IS NULL AND entity["prov:type"] = "prov:Bundle"
      WITH entity
      RETURN entity
      """)
  Mono<EntityNode> findGeneralVersion(@Param("bundleIdentifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"] IS NOT NULL AND entity["prov:type"] = "prov:Bundle"
      WITH entity
      ORDER BY toInteger(entity["pav:version"]) DESC
      LIMIT 1
      RETURN entity
      """)
  Mono<EntityNode> findLastVersion(@Param("bundleIdentifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"] IS NOT NULL AND entity["prov:type"] = "prov:Bundle"
      WITH toInteger(entity["pav:version"]) AS version
      WHERE version IS NOT NULL
      ORDER BY version DESC
      LIMIT 1
      RETURN version
      """)
  Mono<Integer> getLastVersion(@Param("bundleIdentifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WITH DISTINCT entity
      RETURN entity
      """)
  Flux<EntityNode> getAllEntitiesByBundleIdentifier(@Param("bundleIdentifier") String bundleIdentifier);

  @Query("""
      MATCH (token:Entity)-[:was_derived_from]->(version:Entity {identifier: $versionIdentifier})
      WHERE token["prov:type"] = "cpm:Token" AND version["prov:type"] = "prov:Bundle"
      RETURN token
      """)
  Mono<EntityNode> getTokenByVersionEntityIdentifier(@Param("versionIdentifier") String versionIdentifier);

  @Query("""
        MATCH (specific:Entity {identifier: $specificEntityIdentifier})
        MATCH (general:Entity {identifier: $generalEntityIdentifier})
        MERGE (specific)-[:specialization_of]->(general)
        RETURN true
      """)
  Mono<Boolean> createSpecializationOfRelationship(
      @Param("specificEntityIdentifier") String specificEntityIdentifier,
      @Param("generalEntityIdentifier") String generalEntityIdentifier);
}
