package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j;

import java.util.NoSuchElementException;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TrustedPartyRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.neo4j.client.TrustedPartyNeo4jRepositoryClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("live & neo4j")
@Repository
public class TrustedPartyNeo4jRepository implements TrustedPartyRepository {
  private final TrustedPartyNeo4jRepositoryClient client;

  public TrustedPartyNeo4jRepository(
      TrustedPartyNeo4jRepositoryClient client) {
    this.client = client;
  }

  @Override
  public Mono<TrustedPartyNode> save(TrustedPartyNode entity) {
    return client.save(entity);
  }

  @Override
  public Flux<TrustedPartyNode> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<TrustedPartyNode> findByName(String name) {
    return client.findIdByName(name)
        .single()
        .flatMap(client::findById)
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException(
                "TrustedParty with name '" + name + "' has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException(
                "There is more then one TrustedParty with name '" + name + "'!"));
  }

  @Override
  public Mono<TrustedPartyNode> findDefault() {
    return client.findDefaultId()
        .single()
        .flatMap(client::findById)
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException(
                "Default TrustedParty has not been found!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException(
                "There is more then one default TrustedParty!"));
  }

  @Override
  public Mono<TrustedPartyNode> findByOrganizationIdentifier(String organizationIdentifier) {
    return client.findIdByOrganizationIdentifier(organizationIdentifier)
        .single()
        .flatMap(client::findById)
        .onErrorMap(
            NoSuchElementException.class,
            _ -> new NotFoundException(
                "Organization with identifier '" + organizationIdentifier + "' has no TrustedParty!"))
        .onErrorMap(
            IndexOutOfBoundsException.class,
            _ -> new ConflictException(
                "Organization with identifier '" + organizationIdentifier + "' has more then one TrustedParty!"));
  }
}
