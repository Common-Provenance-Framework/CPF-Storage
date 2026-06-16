package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.utils.TrustedPartyUtils;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TrustedPartyRepository;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TrustedPartyService;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
import reactor.core.publisher.Mono;

@Service
public class TrustedPartyServiceImpl implements TrustedPartyService {
  private final TrustedPartyRepository repository;

  public TrustedPartyServiceImpl(TrustedPartyRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<Void> storeTrustedParty(TrustedParty trustedParty) {
    return MONO.<TrustedParty> makeSureNotNullWithMessage("TrustedParty can not be null").apply(trustedParty)
        .flatMap(this.repository::create);
  }

  @Override
  public Mono<TrustedParty> findTrustedParty(TrustedParty trustedParty) {
    return MONO.<TrustedParty> makeSureNotNullWithMessage("TrustedParty can not be null").apply(trustedParty)
        .map(TrustedParty::getName)
        .flatMap(this::getTrustedPartyByName);
  }

  @Override
  public Mono<TrustedParty> getDefaultTrustedParty() {
    return this.repository.findDefault();
  }

  @Override
  public Mono<TrustedParty> getTrustedPartyByName(String name) {
    return MONO.<String> makeSureNotNullWithMessage("TrustedParty name can not be null").apply(name)
        .flatMap(this.repository::findByName);
  }

  @Override
  public Mono<TrustedParty> getTrustedPartyByOrganizationIdentifier(String organizationIdentifier) {
    return MONO.<String> makeSureNotNullWithMessage("Organization identifier can not be null")
        .apply(organizationIdentifier)
        .flatMap(this.repository::findByOrganizationIdentifier);
  }

  @Override
  public Mono<String> getTrustedPartyUrlByOrganizationIdentifier(String organizationIdentifier) {
    return MONO.<String> makeSureNotNullWithMessage("Organization identifier can not be null")
        .apply(organizationIdentifier)
        .flatMap(this.repository::findUrlByOrganizationIdentifier);
  }

  @Override
  public Mono<String> getTrustedPartyUrlByOrganization(Organization organization) {
    return MONO.<Organization> makeSureNotNullWithMessage("Organization can not be null")
        .apply(organization)
        .map(Organization::getIdentifier)
        .flatMap(this::getTrustedPartyUrlByOrganizationIdentifier);
  }

  @Override
  public Mono<Boolean> isRegistered(TrustedParty trustedParty) {
    return Mono.just(trustedParty)
        .map(TrustedParty::getName)
        .flatMap(this.repository::findByName)
        .hasElement()
        .onErrorReturn(NotFoundException.class, false);
  }

  @Override
  public Mono<Boolean> isTrustedPartyValid(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.liftOptionalToMono(Organization::getTrustedParty))
        .map(TrustedParty::getName)
        .flatMap(this.repository::findByName)
        .delayUntil(MONO.liftEffectToMono(TrustedPartyUtils::validate))
        .hasElement()
        .onErrorReturn(NotFoundException.class, false);
  }

}
