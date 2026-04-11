package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.dummy;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.HashMap;
import java.util.Map;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.OrganizationRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("test & dummy")
@Repository
public class OrganizationDummyRepository implements OrganizationRepository {
  private static Map<String, OrganizationNode> organizations = new HashMap<>();

  private void add(OrganizationNode entity) {
    organizations.put(entity.getIdentifier(), entity);
  }

  @Override
  public Mono<OrganizationNode> save(OrganizationNode organization) {
    return MONO.makeSureNotNull(organization)
        .doOnNext(this::add);
  }

  @Override
  public Flux<OrganizationNode> findAll() {
    return Flux.fromIterable(organizations.values());
  }

  @Override
  public Mono<OrganizationNode> findByIdentifier(String identifier) {
    return MONO.makeSureNotNull(identifier)
        .map(organizations::get)
        .flatMap(MONO::makeSureNotNull);
  }

  @Override
  public Mono<Boolean> connectOwns(String organizationIdentifier, String documentIdentifier) {
    return Mono.just(true);
  }

}
