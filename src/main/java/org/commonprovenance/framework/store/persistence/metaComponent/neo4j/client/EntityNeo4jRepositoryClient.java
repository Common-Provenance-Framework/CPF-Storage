package org.commonprovenance.framework.store.persistence.metaComponent.neo4j.client;

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
        MATCH (entity:Entity)
        WHERE entity.identifier = $identifier
        RETURN elementId(entity)
      """)
  Mono<String> getIdByIdentifier(@Param("identifier") String identifier);

  @Query("""
        MATCH (entity:Entity)
        WHERE entity.identifier = $identifier

        OPTIONAL MATCH (entity)-[rRev:revision_of]->(rev:Entity)
        OPTIONAL MATCH (entity)-[rSpec:specialization_of]->(spec:Entity)
        OPTIONAL MATCH (entity)-[rGen:was_generated_by]->(act:Activity)
        OPTIONAL MATCH (entity)-[rAttr:was_attributed_to]->(ag:Agent)
        OPTIONAL MATCH (entity)-[rDer:was_derived_from]->(src:Entity)

        RETURN entity,
          collect(DISTINCT rRev),  collect(DISTINCT rev),
          collect(DISTINCT rSpec), collect(DISTINCT spec),
          collect(DISTINCT rGen),  collect(DISTINCT act),
          collect(DISTINCT rAttr), collect(DISTINCT ag),
          collect(DISTINCT rDer),  collect(DISTINCT src)
      """)
  Mono<EntityNode> findByIdentifierWithRelations(@Param("identifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"] IS NULL AND entity["prov:type"] = "prov:Bundle"
      RETURN entity
      """)
  Mono<EntityNode> findGeneralVersion(@Param("bundleIdentifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"] IS NULL AND entity["prov:type"] = "prov:Bundle"
      RETURN elementId(entity)
      """)
  Mono<String> getGeneralVersionIdByMetaBundleIdentifier(@Param("bundleIdentifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"] IS NOT NULL AND entity["prov:type"] = "prov:Bundle"
      WITH entity
      ORDER BY toInteger(entity["pav:version"]) DESC
      LIMIT 1
      RETURN entity
      """)
  Mono<EntityNode> findLastVersionEntity(@Param("bundleIdentifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"] IS NOT NULL AND entity["prov:type"] = "prov:Bundle"
      WITH entity
      ORDER BY toInteger(entity["pav:version"]) DESC
      LIMIT 1
      RETURN elementId(entity)
      """)
  Mono<String> getLastVersionEntityId(@Param("bundleIdentifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"]=$version AND entity["prov:type"] = "prov:Bundle"
      RETURN elementId(entity);
      """)
  Mono<String> getVersionEntityId(
      @Param("bundleIdentifier") String identifier,
      @Param("version") Integer version);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"] IS NOT NULL AND entity["prov:type"] = "prov:Bundle"
      WITH entity
      ORDER BY toInteger(entity["pav:version"]) DESC
      LIMIT 1
      OPTIONAL MATCH (entity)-[rSpec:specialization_of]->(spec:Entity)
      OPTIONAL MATCH (entity)-[rRev:revision_of]->(rev:Entity)
      RETURN entity,
        collect(DISTINCT rSpec), collect(DISTINCT spec),
        collect(DISTINCT rRev),  collect(DISTINCT rev)
      """)
  Mono<EntityNode> findLastVersionEntityWithRelations(@Param("bundleIdentifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WITH DISTINCT entity

      OPTIONAL MATCH (entity)-[rRev:revision_of]->(rev:Entity)
      OPTIONAL MATCH (entity)-[rSpec:specialization_of]->(spec:Entity)
      OPTIONAL MATCH (entity)-[rGen:was_generated_by]->(act:Activity)
      OPTIONAL MATCH (entity)-[rAttr:was_attributed_to]->(ag:Agent)
      OPTIONAL MATCH (entity)-[rDer:was_derived_from]->(src:Entity)

      RETURN entity,
        collect(DISTINCT rRev),  collect(DISTINCT rev),
        collect(DISTINCT rSpec), collect(DISTINCT spec),
        collect(DISTINCT rGen),  collect(DISTINCT act),
        collect(DISTINCT rAttr), collect(DISTINCT ag),
        collect(DISTINCT rDer),  collect(DISTINCT src)
      """)
  Flux<EntityNode> getAllEntitiesByBundleIdentifier(@Param("bundleIdentifier") String bundleIdentifier);

  @Query("""
      MATCH (token:Entity)-[:was_derived_from]->(version:Entity {identifier: $versionIdentifier})
      WHERE token["prov:type"] = "cpm:Token" AND version["prov:type"] = "prov:Bundle"
      WITH token

      OPTIONAL MATCH (token)-[rAttr:was_attributed_to]->(ag:Agent)
      OPTIONAL MATCH (token)-[rGen:was_generated_by]->(act:Activity)
      OPTIONAL MATCH (token)-[rDer:was_derived_from]->(src:Entity)

      RETURN token,
        collect(DISTINCT rGen),  collect(DISTINCT act),
        collect(DISTINCT rAttr), collect(DISTINCT ag),
        collect(DISTINCT rDer),  collect(DISTINCT src)
        """)
  Mono<EntityNode> getTokenByVersionEntityIdentifier(@Param("versionIdentifier") String versionIdentifier);

  @Query("""
      MATCH (specific:Entity) WHERE elementId(specific)=$specificEntityId
      MATCH (general:Entity) WHERE elementId(general)=$generalEntityId
      MERGE (specific)-[:specialization_of]->(general)
      RETURN true
      """)
  Mono<Boolean> createSpecializationOfRelationship(
      @Param("specificEntityId") String specificEntityId,
      @Param("generalEntityId") String generalEntityId);

  @Query("""
      MATCH (specific:Entity) WHERE elementId(specific)=$specificEntityId
      MATCH (general:Entity) WHERE elementId(general)=$generalEntityId
      MERGE (specific)-[:revision_of]->(general)
      RETURN true
      """)
  Mono<Boolean> createRevisionOfRelationship(
      @Param("specificEntityId") String specificEntityId,
      @Param("generalEntityId") String generalEntityId);

  @Query("""
      MATCH (specific:Entity) WHERE elementId(specific)=$specificEntityId
      MATCH (general:Entity) WHERE elementId(general)=$generalEntityId
      MERGE (specific)-[:was_derived_from]->(general)
        RETURN true
      """)
  Mono<Boolean> createWasDerivedFromRelationship(
      @Param("specificEntityId") String specificEntityId,
      @Param("generalEntityId") String generalEntityId);

  @Query("""
      MATCH (entity:Entity) WHERE elementId(entity)=$entityId
      MATCH (activity:Activity) WHERE elementId(activity)=$activityId
      MERGE (entity)-[:was_generated_by]->(activity)
      RETURN true
      """)
  Mono<Boolean> createWasGeneratedByRelationship(
      @Param("entityId") String entityId,
      @Param("activityId") String activityId);

  @Query("""
      MATCH (entity:Entity) WHERE elementId(entity)=$entityId
      MATCH (agent:Agent) WHERE elementId(agent)=$agentId
      MERGE (entity)-[:was_attributed_to]->(agent)
      RETURN true
      """)
  Mono<Boolean> createWasAttributedToRelationship(
      @Param("entityId") String entityId,
      @Param("agentId") String agentId);
}
