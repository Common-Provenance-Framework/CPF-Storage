package org.commonprovenance.framework.store.web.trustedParty;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface OrganizationWeb {

  Mono<Void> create(Organization organization);

  Mono<Organization> getById(Organization organization);
}
