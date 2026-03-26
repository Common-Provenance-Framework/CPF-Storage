package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface DocumentNeo4jRepositoryClient extends ReactiveNeo4jRepository<DocumentNode, String> {
  @Query("""
          MATCH (document:Document {identifier: $identifier})
          RETURN document
      """)
  Mono<DocumentNode> findByIdentifier(@Param("identifier") String identifier);

}
