package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.DocumentType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface TrustedPartyWeb {
  Mono<TrustedParty> getTrustedParty(Optional<String> optTrustedPartyBaseUrl);

  Function<Organization, Mono<Token>> issueGraphToken(String signature);

  Function<Organization, Mono<Token>> issueGraphToken(DocumentType graphType);

  Function<Organization, Mono<Boolean>> verifySignature(String singature);
}
