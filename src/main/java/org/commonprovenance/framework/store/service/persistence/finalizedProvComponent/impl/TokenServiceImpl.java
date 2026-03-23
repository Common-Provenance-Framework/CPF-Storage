package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl;

import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenPersistence;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TokenService;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TokenServiceImpl implements TokenService {
  private final TokenPersistence persistence;

  public TokenServiceImpl(TokenPersistence persistence) {
    this.persistence = persistence;
  }

  @NotNull
  public Mono<Token> storeToken(@NotNull Token token) {
    return this.persistence.create(token);
  }

  @NotNull
  public Flux<Token> getAllTokens() {
    return this.persistence.getAll();
  }

  @NotNull
  public Mono<Token> getTokenById(@NotNull String id) {
    return this.persistence.getById(id);
  }

  @NotNull
  public Mono<Void> deleteTokenById(@NotNull String id) {
    return this.persistence.deleteById(id);
  }
}