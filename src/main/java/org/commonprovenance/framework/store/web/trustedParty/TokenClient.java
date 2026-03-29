package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Token;
import org.openprovenance.prov.model.QualifiedName;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenClient {

  Function<String, Flux<Token>> getAllByOrganization(Optional<String> trustedPartyUrl);

  Mono<Token> getByDocumentId(
      String organizationId,
      QualifiedName bundle_identifier,
      Format documentFormat,
      Optional<String> trustedPartyUrl);
}
