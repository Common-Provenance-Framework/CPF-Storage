package org.commonprovenance.framework.store.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TrustedPartyPersistence;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.NodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TrustedPartyRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TrustedPartyPersistenceImpl implements TrustedPartyPersistence {

  private final TrustedPartyRepository repository;

  public TrustedPartyPersistenceImpl(
      TrustedPartyRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<TrustedParty> create(TrustedParty trustedParty) {
    return MONO.<TrustedParty>makeSureNotNullWithMessage("TrustedParty can not be 'null'!").apply(trustedParty)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while creating new TrustedParty"))
        .map(ModelFactory::toDomain);
  }

  @Override
  public Mono<TrustedParty> update(TrustedParty trustedParty) {
    return MONO.<TrustedParty>makeSureNotNullWithMessage("TrustedParty can not be 'null'!").apply(trustedParty)
        .flatMap(NodeFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while updating existing TrustedParty"))
        .map(ModelFactory::toDomain);
  }

  @Override
  public Flux<TrustedParty> getAll() {
    return repository.findAll()
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading trusted parties"))
        .map(ModelFactory::toDomain);
  }

  @Override
  public Mono<TrustedParty> getByName(String name) {
    return MONO.<String>makeSureNotNullWithMessage("TrustedParty name can not be 'null'!").apply(name)
        .flatMap(repository::findByName)
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading TrustedParty by name"))
        .switchIfEmpty(Mono.error(new NotFoundException("TrustedParty with name '" + name + "' not found!")))
        .map(ModelFactory::toDomain);
  }

  @Override
  public Mono<TrustedParty> getDefault() {
    return repository.findDefault()
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading default TrustedParty"))
        .map(ModelFactory::toDomain);
  }

  @Override
  public Mono<TrustedParty> getByOrganizationIdentifier(String organizationIdentifier) {
    return MONO.<String>makeSureNotNullWithMessage("Organization identifier can not be 'null'!")
        .apply(organizationIdentifier)
        .flatMap(repository::findByOrganizationIdentifier)
        .onErrorResume(MONO
            .exceptionWrapper("TrustedPartyPersistence - Error while reading TrustedParty by organizationIdentifier"))
        .switchIfEmpty(Mono.error(new NotFoundException(
            "TrustedParty with organizationIdentifier '" + organizationIdentifier + "' not found!")))
        .map(ModelFactory::toDomain);
  }

}
