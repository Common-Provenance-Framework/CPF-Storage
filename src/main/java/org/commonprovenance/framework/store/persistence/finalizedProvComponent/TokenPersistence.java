package org.commonprovenance.framework.store.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Token;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenPersistence {
  Mono<Token> create(Token token);

  Flux<Token> getAll();

  Mono<Token> getByDocumentIdentifier(String documentIdentifier);

  Mono<String> getOrganizationIdentifierByDocumentIdentifier(String documentIdentifier);

}
