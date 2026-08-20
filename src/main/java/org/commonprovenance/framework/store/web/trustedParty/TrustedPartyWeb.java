package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.MetaDocument;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface TrustedPartyWeb {
  Mono<TrustedParty> getTrustedParty(Optional<String> optTrustedPartyBaseUrl);

  Function<Organization, Mono<Token>> issueGraphToken(String signature);

  Function<Organization, Mono<Token>> issueGraphToken(GraphType graphType);

  Mono<Token> issueGraphToken(MetaDocument metaDocument);

  Function<Organization, Mono<Boolean>> verifySignature(String singature);
}
