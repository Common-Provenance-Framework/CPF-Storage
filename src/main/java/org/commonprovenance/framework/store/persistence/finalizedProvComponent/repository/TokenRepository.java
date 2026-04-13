package org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenRepository {
  Mono<TokenNode> save(TokenNode token);

  Flux<TokenNode> findAll();

  Mono<TokenNode> getTokenByDocumentIdentifier(String documentIdentifier);

}
