package org.commonprovenance.framework.store.service.web.trustedParty;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface TrustedPartyService {
  Mono<Organization> createOrganization(Organization organization);

  Mono<Boolean> exists(Organization organization);

  Mono<Boolean> notExists(Organization organization);
}
