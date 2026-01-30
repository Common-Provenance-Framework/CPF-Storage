package org.commonprovenance.framework.store.web.trustedParty;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Token;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TokenClient {
  @NotNull
  Flux<Token> getAll();

  @NotNull
  Mono<Token> getById(@NotNull UUID id);

}
