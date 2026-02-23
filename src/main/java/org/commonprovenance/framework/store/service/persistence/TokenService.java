package org.commonprovenance.framework.store.service.persistence;

import org.commonprovenance.framework.store.model.Token;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenService {
  @NotNull
  Mono<Token> storeToken(@NotNull Token token);

  @NotNull
  Flux<Token> getAllTokens();

  @NotNull
  Mono<Token> getTokenById(@NotNull java.util.UUID id);

  @NotNull
  Mono<Void> deleteTokenById(@NotNull java.util.UUID id);
}
