package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DocumentNeo4jRepositoryClient extends ReactiveNeo4jRepository<DocumentNode, String> {
  @Query("""
      MATCH (document:Document)
      WHERE document.identifier = $identifier
      RETURN elementId(document) AS id
      """)
  Flux<String> getIdByIdentifier(@Param("identifier") String identifier);

  @Query("""
      RETURN EXISTS {
        MATCH (:Document {identifier: $identifier})
      } AS exists
      """)
  Mono<Boolean> existsByIdentifier(@Param("identifier") String identifier);

  @Query("""
      MATCH (document:Document)
      WHERE document.identifier = $identifier
      RETURN count(document) as occurence
      """)
  Mono<Integer> countByIdentifier(@Param("identifier") String identifier);

}
