package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface BundleNeo4jRepositoryClient extends ReactiveNeo4jRepository<BundleNode, String> {

  @Query("MATCH (bundle:Bundle) -[r:bundle_entities]->(entity:Entity {id: $generalEntityId}) RETURN bundle")

  Mono<BundleNode> getBundleByGeneralEntity(@Param("generalEntityId") String generalEntityId);
}
