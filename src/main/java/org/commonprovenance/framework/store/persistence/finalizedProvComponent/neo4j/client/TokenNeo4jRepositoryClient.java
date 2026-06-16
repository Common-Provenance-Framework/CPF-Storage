package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TokenNeo4jRepositoryClient extends ReactiveNeo4jRepository<TokenNode, String> {
  @Query("""
      MATCH (document:Document)-[:has_token]->(token:Token)
      WHERE document.identifier = $documentIdentifier
      RETURN elementId(token) as id
      """)
  Flux<String> findTokenIdsByDocumentIdentifier(@Param("documentIdentifier") String documentIdentifier);

  @Query("""
        MATCH (token:Token)
        MATCH (trustedParty:TrustedParty)
        WHERE trustedParty.name = $trustedPartyName AND elementId(token) = $tokenId
        MERGE (token)-[:was_issued_by]->(trustedParty)
        RETURN true
      """)
  Mono<Boolean> createWasIssuedByRelationship(
      @Param("tokenId") String tokenId,
      @Param("trustedPartyName") String trustedPartyName);
}
