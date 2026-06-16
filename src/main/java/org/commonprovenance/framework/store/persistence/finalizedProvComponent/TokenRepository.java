package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface TokenRepository {
  Mono<Void> save(Token token);

  Mono<Token> getTokenByDocumentIdentifier(String documentIdentifier);

  Mono<String> getTokenIdByDocumentIdentifier(String documentIdentifier);

  Function<Document, Mono<Void>> connectWasIssuedBy(Optional<TrustedParty> maybeTrustedParty);

}
