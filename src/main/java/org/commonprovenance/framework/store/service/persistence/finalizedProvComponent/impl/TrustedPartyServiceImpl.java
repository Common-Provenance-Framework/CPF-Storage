package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TrustedPartyPersistence;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TrustedPartyService;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class TrustedPartyServiceImpl implements TrustedPartyService {
  private final TrustedPartyPersistence persistence;

  public TrustedPartyServiceImpl(TrustedPartyPersistence persistence) {
    this.persistence = persistence;
  }

  @Override
  public Mono<TrustedParty> storeTrustedParty(TrustedParty trustedParty) {
    return MONO.<TrustedParty> makeSureNotNullWithMessage("TrustedParty can not be null").apply(trustedParty)
        .flatMap(this.persistence::create);
  }

  @Override
  public Mono<TrustedParty> findTrustedParty(TrustedParty trustedParty) {
    return MONO.<TrustedParty> makeSureNotNullWithMessage("TrustedParty can not be null").apply(trustedParty)
        .map(TrustedParty::getName)
        .flatMap(this::getTrustedPartyByName);
  }

  @Override
  public Mono<TrustedParty> getDefaultTrustedParty() {
    return this.persistence.getDefault();
  }

  @Override
  public Mono<TrustedParty> getTrustedPartyByName(String name) {
    return MONO.<String> makeSureNotNullWithMessage("TrustedParty name can not be null").apply(name)
        .flatMap(this.persistence::getByName);
  }

  @Override
  public Mono<TrustedParty> getTrustedPartyByOrganizationIdentifier(String organizationIdentifier) {
    return MONO.<String> makeSureNotNullWithMessage("Organization identifier can not be null")
        .apply(organizationIdentifier)
        .flatMap(this.persistence::getByOrganizationIdentifier);
  }

  @Override
  public Mono<String> getTrustedPartyUrlByOrganizationIdentifier(String organizationIdentifier) {
    return MONO.<String> makeSureNotNullWithMessage("Organization identifier can not be null")
        .apply(organizationIdentifier)
        .flatMap(this.persistence::getUrlByOrganizationIdentifier);
  }

  @Override
  public Mono<String> getTrustedPartyUrlByOrganization(Organization organization) {
    return MONO.<Organization> makeSureNotNullWithMessage("Organization can not be null")
        .apply(organization)
        .map(Organization::getIdentifier)
        .flatMap(this::getTrustedPartyUrlByOrganizationIdentifier);
  }

}
