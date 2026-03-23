package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.TrustedParty;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

public interface TrustedPartyService {
  @NotNull
  Mono<TrustedParty> storeTrustedParty(@NotNull TrustedParty trustedParty);

  @NotNull
  Mono<TrustedParty> findTrustedParty(@NotNull TrustedParty trustedParty);

  @NotNull
  Mono<TrustedParty> getDefaultTrustedParty();

  @NotNull
  Mono<TrustedParty> getTrustedPartyById(@NotNull String id);

  @NotNull
  Mono<TrustedParty> getTrustedPartyByName(@NotNull String name);
}
