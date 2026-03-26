package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface OrganizationNeo4jRepositoryClient extends ReactiveNeo4jRepository<OrganizationNode, String> {
  @Query("""
          MATCH (organization:Organization {identifier: $identifier})
          RETURN organization
      """)
  Mono<OrganizationNode> findByIdentifier(@Param("identifier") String identifier);

}
