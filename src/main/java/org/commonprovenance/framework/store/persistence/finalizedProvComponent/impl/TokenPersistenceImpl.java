package org.commonprovenance.framework.store.persistence.finalizedProvComponent.impl;

import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenPersistence;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.TokenRepository;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class TokenPersistenceImpl implements TokenPersistence {

  private final TokenRepository repository;

  public TokenPersistenceImpl(
      TokenRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<Void> create(Token token) {
    return repository.save(token);
  }

  @Override
  public Mono<Token> getByDocumentIdentifier(String documentIdentifier) {
    return repository.getTokenByDocumentIdentifier(documentIdentifier);
  }

}
