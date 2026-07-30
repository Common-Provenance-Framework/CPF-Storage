package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Token;
import org.openprovenance.prov.model.QualifiedName;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenWeb {

  Function<String, Flux<Token>> getAllByOrganization(Optional<String> optTrustedPartyBaseUrl);

  Mono<Token> getByDocumentId(
      String organizationId,
      QualifiedName bundleIdentifier,
      GraphFormat documentFormat,
      Optional<String> optTrustedPartyBaseUrl);
}
