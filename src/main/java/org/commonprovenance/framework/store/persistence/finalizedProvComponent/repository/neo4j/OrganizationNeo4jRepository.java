package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.OrganizationRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client.OrganizationNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class OrganizationNeo4jRepository implements OrganizationRepository {
  private final OrganizationNeo4jRepositoryClient client;

  public OrganizationNeo4jRepository(
      OrganizationNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<OrganizationNode> save(OrganizationNode organization) {
    return client.save(organization);
  }

  @Override
  public Flux<OrganizationNode> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<OrganizationNode> findByIdentifier(String identifier) {
    return client.findByIdentifier(identifier);
  }

  @Override
  public Mono<Boolean> connectOwns(String organizationIdentifier, String documentIdentifier) {
    return client.createOwnsRelationship(organizationIdentifier, documentIdentifier);
  }

}
