package org.commonprovenance.framework.store.web.trustedParty.impl;

import java.util.Optional;

import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyClient;
import org.commonprovenance.framework.store.web.trustedParty.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TrustedPartyTPResponseDTO;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class TrustedPartyClientImpl implements TrustedPartyClient {
  private final Client client;

  public TrustedPartyClientImpl(
      Client client) {
    this.client = client;
  }

  private WebClient buildWebClient(String trustedPartyUrl) {
    return WebClient.builder()
        .baseUrl(trustedPartyUrl)
        .defaultHeader("Accept", "application/json")
        .build();
  }

  @Override
  public Mono<TrustedParty> getInfo(Optional<String> trustedPartyUrl) {
    return Mono.justOrEmpty(trustedPartyUrl)
        .map(this::buildWebClient)
        .flatMap(this.client.sendCustomGetOneRequest("/info", TrustedPartyTPResponseDTO.class))
        .switchIfEmpty(this.client.sendGetOneRequest("/info", TrustedPartyTPResponseDTO.class))
        .flatMap(ModelFactory.toDomain(trustedPartyUrl.orElse(this.client.getUrl())));
  }

}
