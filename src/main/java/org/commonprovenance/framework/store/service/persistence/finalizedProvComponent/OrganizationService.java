package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationService {

  Mono<Organization> storeOrganization(Organization organization);

  Mono<Organization> updateOrganization(Organization organization);

  Mono<Boolean> exists(Organization organization);

  Mono<Boolean> notExists(Organization organization);

  Flux<Organization> getAllOrganizations();

  Mono<Organization> getOrganizationByIdentifier(String identifier);

}
