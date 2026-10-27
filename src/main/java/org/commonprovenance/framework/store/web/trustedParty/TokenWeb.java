package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Token;
import org.openprovenance.prov.model.QualifiedName;

import reactor.core.publisher.Flux;

public interface TokenWeb {

  Function<String, Flux<Token>> getAllByOrganization(Optional<String> optTrustedPartyBaseUrl);

  Flux<Token> getByDocumentId(
      String organizationId,
      QualifiedName bundleIdentifier,
      GraphFormat graphFormat,
      Optional<String> optTrustedPartyBaseUrl);
}
