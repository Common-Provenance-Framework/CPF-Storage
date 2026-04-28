package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface BundleNeo4jRepositoryClient extends ReactiveNeo4jRepository<BundleNode, String> {

  @Query("""
      MATCH (bundle:Bundle)
      WHERE bundle.identifier = $identifier
      RETURN elementId(bundle) AS id
      """)
  Mono<String> getIdByIdentifier(@Param("identifier") String identifier);

  @Query("""
      RETURN EXISTS {
        MATCH (bundle:Bundle) WHERE bundle.identifier = $identifier
      }
      """)
  Mono<Boolean> existsByIdentifier(@Param("identifier") String identifier);

  @Query("""
        MATCH (bundle:Bundle)
        WHERE bundle.identifier = $identifier
        RETURN bundle
      """)
  Mono<BundleNode> findByIdentifier(@Param("identifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle)-[:bundle_entities]->(entity:Entity {identifier: $generalEntityIdentifier})
      RETURN bundle
      """)
  Mono<BundleNode> getBundleByGeneralEntity(@Param("generalEntityIdentifier") String generalEntityIdentifier);

  @Query("""
      MATCH (bundle:Bundle {identifier: $identifier})
      RETURN EXISTS {
        MATCH (bundle)-[:bundle_entities]->(entity:Entity)
        WHERE entity["prov:type"] = "prov:Bundle"
          AND entity["pav:version"] IS NOT NULL
      }
      """)
  Mono<Boolean> hasVersionEntity(@Param("identifier") String identifier);

  @Query("""
        MATCH (bundle:Bundle {identifier: $bundleIdentifier})
        MATCH (entity:Entity {identifier: $entityIdentifier})
        MERGE (bundle)-[:bundle_entities]->(entity)
        RETURN true
      """)
  Mono<Boolean> createBundleEntitiesRelationship(
      @Param("bundleIdentifier") String bundleIdentifier,
      @Param("entityIdentifier") String entityIdentifier);
}
