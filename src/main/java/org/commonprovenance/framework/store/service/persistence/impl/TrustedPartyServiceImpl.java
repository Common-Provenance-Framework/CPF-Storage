package org.commonprovenance.framework.store.service.persistence.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.TrustedPartyPersistence;
import org.commonprovenance.framework.store.service.persistence.TrustedPartyService;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

@Service
public class TrustedPartyServiceImpl implements TrustedPartyService {
  private final TrustedPartyPersistence persistence;

  public TrustedPartyServiceImpl(TrustedPartyPersistence persistence) {
    this.persistence = persistence;
  }

  @Override
  @NotNull
  public Mono<TrustedParty> storeTrustedParty(@NotNull TrustedParty trustedParty) {
    return MONO.<TrustedParty>makeSureNotNullWithMessage("TrustedParty can not be null").apply(trustedParty)
        .flatMap(this.persistence::create);
  }

  @Override
  public @NotNull Mono<TrustedParty> findTrustedParty(@NotNull TrustedParty trustedParty) {
    return MONO.<TrustedParty>makeSureNotNullWithMessage("TrustedParty can not be null").apply(trustedParty)
        .map(TrustedParty::getId)
        .flatMap(Mono::justOrEmpty)
        .flatMap(this::getTrustedPartyById)
        .switchIfEmpty(this.getTrustedPartyByName(trustedParty.getName()));
  }

  @Override
  @NotNull
  public Mono<TrustedParty> getDefaultTrustedParty() {
    return this.persistence.getDefault();
  }

  @Override
  @NotNull
  public Mono<TrustedParty> getTrustedPartyById(@NotNull UUID id) {
    return MONO.<UUID>makeSureNotNullWithMessage("TrustedParty id can not be null").apply(id)
        .flatMap(this.persistence::getById);
  }

  @Override
  public @NotNull Mono<TrustedParty> getTrustedPartyByName(@NotNull String name) {
    return MONO.<String>makeSureNotNullWithMessage("TrustedParty name can not be null").apply(name)
        .flatMap(this.persistence::getByName);
  }

}