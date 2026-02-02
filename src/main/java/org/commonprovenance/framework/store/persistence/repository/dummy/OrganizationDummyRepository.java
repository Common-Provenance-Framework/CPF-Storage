package org.commonprovenance.framework.store.persistence.repository.dummy;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.HashMap;
import java.util.Map;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.persistence.entity.OrganizationEntity;
import org.commonprovenance.framework.store.persistence.repository.OrganizationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("test & dummy")
@Repository
public class OrganizationDummyRepository implements OrganizationRepository {
  private static Map<String, OrganizationEntity> organizations = new HashMap<>();

  private void add(OrganizationEntity entity) {
    organizations.put(entity.getId(), entity);
  }

  @Override
  public Mono<OrganizationEntity> save(OrganizationEntity entity) {
    return MONO.makeSureNotNull(entity)
        .doOnNext(this::add);
  }

  @Override
  public Flux<OrganizationEntity> findAll() {
    return Flux.fromIterable(organizations.values());
  }

  @Override
  public Mono<OrganizationEntity> findById(String id) {
    return MONO.makeSureNotNull(id)
        .map(organizations::get)
        .flatMap(MONO::makeSureNotNull);
  }

  @Override
  public Mono<OrganizationEntity> findByName(String name) {
    return MONO.makeSureNotNull(name)
        .thenMany(this.findAll())
        .filter(o -> o.getName().equals(name))
        .singleOrEmpty()
        .onErrorResume(MONO.exceptionWrapper("OrganizationPersistence - Error while reading organization by name"))
        .switchIfEmpty(Mono.error(new NotFoundException("Organization with name '" + name + "' not found!")));
  }

  @Override
  public Mono<Void> deleteById(String id) {
    return MONO.makeSureNotNull(id)
        .map(organizations::remove)
        .then();
  }
}
