package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl;

import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenPersistence;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TokenService;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TokenServiceImpl implements TokenService {
  private final TokenPersistence persistence;

  public TokenServiceImpl(TokenPersistence persistence) {
    this.persistence = persistence;
  }

  @Override
  public Mono<Token> storeToken(Token token) {
    return this.persistence.create(token);
  }

  @Override
  public Flux<Token> getAllTokens() {
    return this.persistence.getAll();
  }

  @Override
  public Mono<Token> getByDocumentIdentifier(String documentIdentifier) {
    return this.persistence.getByDocumentIdentifier(documentIdentifier);
  }
}