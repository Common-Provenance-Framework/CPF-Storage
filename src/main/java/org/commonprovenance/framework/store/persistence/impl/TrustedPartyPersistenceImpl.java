package org.commonprovenance.framework.store.persistence.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.persistence.OrganizationPersistence;
import org.commonprovenance.framework.store.persistence.TrustedPartyPersistence;
import org.commonprovenance.framework.store.persistence.entity.factory.EntityFactory;
import org.commonprovenance.framework.store.persistence.repository.OrganizationRepository;
import org.commonprovenance.framework.store.persistence.repository.TrustedPartyRepository;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
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
  @NotNull
  public Mono<TrustedParty> create(@NotNull TrustedParty trustedParty) {
    return MONO.<TrustedParty>makeSureNotNullWithMessage("TrustedParty can not be 'null'!").apply(trustedParty)
        .flatMap(EntityFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while creating new TrustedParty"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<TrustedParty> update(@NotNull TrustedParty trustedParty) {
    return MONO.<TrustedParty>makeSureNotNullWithMessage("TrustedParty can not be 'null'!").apply(trustedParty)
        .flatMap(EntityFactory::toEntity)
        .flatMap(repository::save)
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while updating existing TrustedParty"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Flux<TrustedParty> getAll() {
    return repository.findAll()
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading trusted parties"))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Mono<TrustedParty> getById(@NotNull UUID id) {
    return MONO.<UUID>makeSureNotNullWithMessage("TrustedParty Id can not be 'null'!").apply(id)
        .map(UUID::toString)
        .flatMap(repository::findById)
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading trusted party"))
        .switchIfEmpty(Mono.error(new NotFoundException("TrustedParty with id '" + id + "' not found!")))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<TrustedParty> getByName(@NotNull String name) {
    return MONO.<String>makeSureNotNullWithMessage("TrustedParty name can not be 'null'!").apply(name)
        .flatMap(repository::findByName)
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading TrustedParty by name"))
        .switchIfEmpty(Mono.error(new NotFoundException("TrustedParty with name '" + name + "' not found!")))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  @NotNull
  public Mono<Void> deleteById(@NotNull UUID uuid) {
    return MONO.<UUID>makeSureNotNullWithMessage("TrustedParty Id can not be 'null'!").apply(uuid)
        .map(UUID::toString)
        .flatMap(repository::deleteById)
        .onErrorResume(MONO.exceptionWrapper("TrustedPartyPersistence - Error while reading TrustedParty"));
  }

}
