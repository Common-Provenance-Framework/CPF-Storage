package org.commonprovenance.framework.store.service.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyService;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationClient;

import reactor.core.publisher.Mono;

public class TrustedPartyServiceImpl implements TrustedPartyService {
  private final OrganizationClient organizationClient;

  public TrustedPartyServiceImpl(OrganizationClient organizationClient) {
    this.organizationClient = organizationClient;
  }

  @Override
  public Mono<Organization> createOrganization(Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be null!").apply(organization)
        .flatMap(MONO.<Organization>makeSureAsync(this::notExists, "Organization already registered!"))
        .flatMap(this.organizationClient::create);
  }

  @Override
  public Mono<Boolean> exists(Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be null!").apply(organization)
        .map(Organization::getId)
        .flatMap(Mono::justOrEmpty)
        .flatMap(this.organizationClient::getById)
        .switchIfEmpty(this.organizationClient.getByName(organization.getName()))
        .thenReturn(true)
        .switchIfEmpty(Mono.just(false))
        .onErrorResume(NotFoundException.class, _ -> Mono.just(false));
  }

  @Override
  public Mono<Boolean> notExists(Organization organization) {
    return this.exists(organization).map(exists -> !exists);
  }
}
