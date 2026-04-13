package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Token;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenService {

  Mono<Token> storeToken(Token token);

  Flux<Token> getAllTokens();

  Mono<Token> getByDocumentIdentifier(String documentIdentifier);

}
