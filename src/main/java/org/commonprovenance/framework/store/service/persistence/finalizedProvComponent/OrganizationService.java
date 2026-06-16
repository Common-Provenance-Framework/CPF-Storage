package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface OrganizationService {

  Mono<Void> storeOrganization(Organization organization);

  Mono<Void> updateOrganization(Organization organization);

  Mono<Boolean> exists(Organization organization);

  Mono<Boolean> notExists(Organization organization);

  Mono<Void> checkOrganizationDoesNotExists(Organization organization);

  Mono<Void> checkOrganizationExists(Organization organization);

  Mono<Organization> getOrganizationByIdentifier(String identifier);

  Mono<Organization> getOrganization(Organization organization);

  Mono<Void> storeDocument(Organization organization);

}
