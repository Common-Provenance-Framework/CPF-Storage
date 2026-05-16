package org.commonprovenance.framework.store.persistence.finalizedProvComponent.impl;

import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TrustedPartyPersistence;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TrustedPartyRepository;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class TrustedPartyPersistenceImpl implements TrustedPartyPersistence {

  private final TrustedPartyRepository repository;

  public TrustedPartyPersistenceImpl(
      TrustedPartyRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<Void> create(TrustedParty trustedParty) {
    return repository.create(trustedParty);
  }

  @Override
  public Mono<TrustedParty> getByName(String name) {
    return repository.findByName(name);

  }

  @Override
  public Mono<TrustedParty> getDefault() {
    return repository.findDefault();
  }

  @Override
  public Mono<TrustedParty> getByOrganizationIdentifier(String organizationIdentifier) {
    return repository.findByOrganizationIdentifier(organizationIdentifier);
  }

  @Override
  public Mono<String> getUrlByOrganizationIdentifier(String organizationIdentifier) {
    return repository.findUrlByOrganizationIdentifier(organizationIdentifier);
  }

}
