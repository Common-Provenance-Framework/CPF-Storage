package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j;

import java.util.NoSuchElementException;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
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
    return client.getIdByIdentifier(identifier)
        .single()
        .flatMap(client::findById)
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException("Organization with identifier '" + identifier + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException("There is more then one organization with identifier '" + identifier + "'!"));
  }

  @Override
  public Mono<Boolean> connectOwns(String organizationIdentifier, String documentIdentifier) {
    return client.createOwnsRelationship(organizationIdentifier, documentIdentifier);
  }

}
