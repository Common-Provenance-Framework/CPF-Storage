package org.commonprovenance.framework.store.service.persistence.impl;

import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.TokenPersistence;
import org.commonprovenance.framework.store.service.persistence.TokenService;
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
  public Mono<Token> getTokenById(@NotNull java.util.UUID id) {
    return this.persistence.getById(id);
  }

  @NotNull
  public Mono<Void> deleteTokenById(@NotNull java.util.UUID id) {
    return this.persistence.deleteById(id);
  }
}