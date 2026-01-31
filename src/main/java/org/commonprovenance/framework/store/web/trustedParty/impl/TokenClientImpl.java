package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.web.trustedParty.TokenClient;
import org.commonprovenance.framework.store.web.trustedParty.client.TrustedPartyClient;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.mapper.DomainMapper;
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

  private Mono<TokenResponseDTO> getOneReq(String id) {
    return trustedPartyClient.sendGetOneRequest("/tokens/" + id,
        TokenResponseDTO.class);
  }

  private Flux<TokenResponseDTO> getManyReq() {
    return trustedPartyClient.sendGetManyRequest("/tokens", TokenResponseDTO.class);
  }

  @Override
  public @NotNull Flux<Token> getAll() {
    return getManyReq()
        .flatMap(DomainMapper::toDomain);
  }

  @Override
  public @NotNull Mono<Token> getById(@NotNull UUID id) {
    return Mono.just(id)
        .flatMap(MONO.makeSureNotNullWithMessage("Token id can not be null!"))
        .map(UUID::toString)
        .flatMap(this::getOneReq)
        .flatMap(DomainMapper::toDomain);
  }
}
