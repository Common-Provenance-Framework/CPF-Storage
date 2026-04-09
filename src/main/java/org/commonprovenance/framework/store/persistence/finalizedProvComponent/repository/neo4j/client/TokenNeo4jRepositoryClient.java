package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

@Repository
public interface TokenNeo4jRepositoryClient extends ReactiveNeo4jRepository<TokenNode, String> {
  @Query("""
      MATCH (token:Token)-[:belongs_to]->(document:Document)
      WHERE document.identifier = $documentIdentifier
      RETURN elementId(token) as id
      """)
  Flux<String> findTokenIdsByDocumentIdentifier(@Param("documentIdentifier") String documentIdentifier);

  @Query("""
      MATCH (token:Token)-[:belongs_to]->(document:Document)
      WHERE document.identifier = $documentIdentifier
      RETURN token.organization_identifier as organizationIdentifier
      """)
  Flux<String> findOrganizationIdentifierByDocumentIdentifier(@Param("documentIdentifier") String documentIdentifier);
}
