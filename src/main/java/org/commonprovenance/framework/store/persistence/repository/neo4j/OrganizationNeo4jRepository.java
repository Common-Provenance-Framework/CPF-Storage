package org.commonprovenance.framework.store.persistence.repository.neo4j;

import org.commonprovenance.framework.store.persistence.entity.OrganizationEntity;
import org.commonprovenance.framework.store.persistence.repository.OrganizationRepository;
import org.commonprovenance.framework.store.persistence.repository.neo4j.client.OrganizationNeo4jRepositoryClient;
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
  public Mono<OrganizationEntity> save(OrganizationEntity entity) {
    return client.save(entity);
  }

  @Override
  public Flux<OrganizationEntity> findAll() {
    return client.findAll();
  }

  @Override
  public Mono<OrganizationEntity> findById(String id) {
    return client.findById(id);
  }

  @Override
  public Mono<OrganizationEntity> findByName(String name) {
    return client.findByName(name);
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return client.deleteById(id);
  }
}
