package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface TrustedPartyClient {
  Mono<TrustedParty> getInfo(Optional<String> trustedPartyUrl);

  Mono<Token> issueToken(
      Organization organization,
      Document document,
      GraphType type,
      Optional<String> trustedPartyUrl);

  Mono<Boolean> verifySignature(
      Organization organization,
      Document document,
      Optional<String> trustedPartyUrl);
}
