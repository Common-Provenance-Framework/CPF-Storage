package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Token;

import reactor.core.publisher.Mono;

public interface TokenPersistence {
  Mono<Void> create(Token token);

  Mono<Token> getByDocumentIdentifier(String documentIdentifier);

}
