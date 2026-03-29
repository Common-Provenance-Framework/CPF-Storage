package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationPersistence {

  Mono<Organization> create(Organization organization);

  Mono<Organization> update(Organization organization);

  Flux<Organization> getAll();

  Mono<Organization> getByIdentifier(String identifier);

}
