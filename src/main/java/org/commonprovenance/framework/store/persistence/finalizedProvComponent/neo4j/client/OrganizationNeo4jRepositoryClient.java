package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrganizationNeo4jRepositoryClient extends ReactiveNeo4jRepository<OrganizationNode, String> {
  @Query("""
        MATCH (organization:Organization)
        WHERE organization.identifier = $identifier
        RETURN elementId(organization) AS id
      """)
  Flux<String> getIdByIdentifier(@Param("identifier") String identifier);

  @Query("""
        MATCH (organization:Organization)
        WHERE organization.identifier = $identifier
        RETURN organization
      """)
  Flux<OrganizationNode> getByIdentifier(@Param("identifier") String identifier);

  @Query("""
        MATCH (organization:Organization {identifier: $organizationIdentifier})
        MATCH (trustedParty:TrustedParty {name: $name})
        MERGE (organization)-[:trusts]->(trustedParty)
        RETURN true
      """)
  Mono<Boolean> createTrustsRelationship(
      @Param("organizationIdentifier") String organizationIdentifier,
      @Param("name") String name);

  @Query("""
        MATCH (organization:Organization {identifier: $organizationIdentifier})
        MATCH (document:Document {identifier: $documentIdentifier})
        MERGE (organization)-[:owns]->(document)
        RETURN true
      """)
  Mono<Boolean> createOwnsRelationship(
      @Param("organizationIdentifier") String organizationIdentifier,
      @Param("documentIdentifier") String documentIdentifier);

  @Query("""
      RETURN EXISTS {
        MATCH (:Organization {identifier: $identifier})
      } AS exists
      """)
  Mono<Boolean> existsByIdentifier(@Param("identifier") String identifier);

  @Query("""
      MATCH (organization:Organization)
      WHERE organization.identifier = $identifier
      RETURN count(organization) as occurence
      """)
  Mono<Integer> countByIdentifier(@Param("identifier") String identifier);

}
