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

        OPTIONAL MATCH (bundle)-[rEntity:bundle_entities]->(e:Entity)
        OPTIONAL MATCH (bundle)-[rActivity:bundle_activities]->(a:Activity)
        OPTIONAL MATCH (bundle)-[rAgent:bundle_agents]->(ag:Agent)

        RETURN bundle,
          collect(DISTINCT rEntity),  collect(DISTINCT e),
          collect(DISTINCT rActivity), collect(DISTINCT a),
          collect(DISTINCT rAgent),  collect(DISTINCT ag)
      """)
  Mono<BundleNode> findByIdentifier(@Param("identifier") String identifier);

  @Query("""
      MATCH (bundle:Bundle)-[:bundle_entities]->(entity:Entity {identifier: $generalEntityIdentifier})
      WITH DISTINCT bundle

      OPTIONAL MATCH (bundle)-[rEntity:bundle_entities]->(e:Entity)
      OPTIONAL MATCH (bundle)-[rActivity:bundle_activities]->(a:Activity)
      OPTIONAL MATCH (bundle)-[rAgent:bundle_agents]->(ag:Agent)

      RETURN bundle,
        collect(DISTINCT rEntity),   collect(DISTINCT e),
        collect(DISTINCT rActivity), collect(DISTINCT a),
        collect(DISTINCT rAgent),    collect(DISTINCT ag)
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
}
