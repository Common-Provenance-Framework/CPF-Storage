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
  Mono<EntityNode> findByIdentifier(@Param("identifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"] IS NULL AND entity["prov:type"] = "prov:Bundle"
      WITH entity
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
  Mono<EntityNode> findGeneralVersion(@Param("bundleIdentifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $bundleIdentifier})-[:bundle_entities]->(entity:Entity)
      WHERE entity["pav:version"] IS NOT NULL AND entity["prov:type"] = "prov:Bundle"
      WITH entity
      ORDER BY toInteger(entity["pav:version"]) DESC
      LIMIT 1
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
}
