package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository;

import org.commonprovenance.framework.store.model.Token;

import reactor.core.publisher.Mono;

public interface TokenRepository {
  Mono<Void> save(Token token);

  Mono<Token> getTokenByDocumentIdentifier(String documentIdentifier);

}
