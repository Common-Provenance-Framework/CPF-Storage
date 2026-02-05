package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.TokenClient;
import org.commonprovenance.framework.store.web.trustedParty.client.TrustedPartyClient;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TokenClientImpl implements TokenClient {
  private final TrustedPartyClient trustedPartyClient;

  public TokenClientImpl(
      TrustedPartyClient client) {
    this.trustedPartyClient = client;
  }

  private Mono<TokenTPResponseDTO> getOneReq(String id) {
    return trustedPartyClient.sendGetOneRequest("/tokens/" + id,
        TokenTPResponseDTO.class);
  }

  private Flux<TokenTPResponseDTO> getManyReq() {
    return trustedPartyClient.sendGetManyRequest("/tokens", TokenTPResponseDTO.class);
  }

  @Override
  public @NotNull Flux<Token> getAll() {
    return getManyReq()
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Token> getById(@NotNull UUID id) {
    return Mono.just(id)
        .flatMap(MONO.makeSureNotNullWithMessage("Token id can not be null!"))
        .map(UUID::toString)
        .flatMap(this::getOneReq)
        .flatMap(ModelFactory::toDomain);
  }
}
