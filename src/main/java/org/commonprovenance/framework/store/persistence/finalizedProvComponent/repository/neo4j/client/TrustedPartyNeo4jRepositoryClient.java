package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

@Repository
public interface TrustedPartyNeo4jRepositoryClient extends ReactiveNeo4jRepository<TrustedPartyNode, String> {

  @Query("""
        MATCH (trustedParty:TrustedParty)
        WHERE trustedParty.name = $name
        RETURN elementId(trustedParty) as id
      """)
  Flux<String> findIdByName(@Param("name") String name);

  @Query("""
        MATCH (trustedParty:TrustedParty)
        WHERE trustedParty.is_default = TRUE
        RETURN elementId(trustedParty) as id
      """)
  Flux<String> findDefaultId();

  @Query("""
        MATCH (organization:Organization)-[:trusts]->(trustedParty:TrustedParty)
        WHERE organization.identifier = $organizationIdentifier
        RETURN elementId(trustedParty) as id
      """)
  Flux<String> findIdByOrganizationIdentifier(@Param("organizationIdentifier") String organizationIdentifier);
}
