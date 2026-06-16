package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl;

import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenRepository;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TokenService;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class TokenServiceImpl implements TokenService {
  private final TokenRepository repository;

  public TokenServiceImpl(TokenRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<Void> storeToken(Token token) {
    return this.repository.save(token);
  }

  @Override
  public Mono<Token> getByDocumentIdentifier(String documentIdentifier) {
    return this.repository.getTokenByDocumentIdentifier(documentIdentifier);
  }
}
