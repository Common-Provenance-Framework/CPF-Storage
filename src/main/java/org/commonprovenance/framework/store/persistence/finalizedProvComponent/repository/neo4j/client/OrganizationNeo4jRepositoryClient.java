package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface OrganizationNeo4jRepositoryClient extends ReactiveNeo4jRepository<OrganizationNode, String> {

  Mono<OrganizationNode> findByName(String name);
}
