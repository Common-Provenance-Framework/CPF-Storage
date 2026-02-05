package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;

import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface TrustedPartyClient {
  Mono<TrustedParty> getInfo(Optional<String> trustedPartyUrl);
}
