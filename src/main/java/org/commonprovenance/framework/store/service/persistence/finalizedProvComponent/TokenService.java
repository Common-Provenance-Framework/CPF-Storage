package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent;

import org.commonprovenance.framework.store.model.Token;

import reactor.core.publisher.Mono;

public interface TokenService {

  Mono<Void> storeToken(Token token);

  Mono<Token> getByDocumentIdentifier(String documentIdentifier);

}
