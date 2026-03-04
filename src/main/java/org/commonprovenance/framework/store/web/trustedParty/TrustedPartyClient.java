package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface TrustedPartyClient {
  Mono<TrustedParty> getInfo(Optional<String> trustedPartyUrl);

  Function<Document, Mono<Token>> issueGraphToken(Optional<String> trustedPartyUrl);

  Function<Document, Mono<Boolean>> verifySignature(Organization organization);
}
