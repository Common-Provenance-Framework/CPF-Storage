package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client;

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
        MATCH (o:Organization {identifier: $organizationIdentifier})
        MATCH (d:Document {identifier: $documentIdentifier})
        MERGE (o)-[:owns]->(d)
        RETURN true
      """)
  Mono<Boolean> createOwnsRelationship(
      @Param("organizationIdentifier") String organizationIdentifier,
      @Param("documentIdentifier") String documentIdentifier);

}
