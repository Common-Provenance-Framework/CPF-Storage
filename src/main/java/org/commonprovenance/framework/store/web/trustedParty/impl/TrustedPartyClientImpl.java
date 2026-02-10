package org.commonprovenance.framework.store.web.trustedParty.impl;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyClient;
import org.commonprovenance.framework.store.web.trustedParty.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.DTOFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
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

  @Override
  public Mono<Token> issueToken(
      Organization organization,
      Document document,
      GraphType type,
      Optional<String> trustedPartyUrl) {
    return DTOFactory.toForm(organization, document, type)
        .flatMap(trustedPartyUrl
            .map(this::buildWebClient)
            .map(this.client.sendCustomPostRequest("/issueToken", TokenTPResponseDTO.class))
            .orElse(this.client.sendPostRequest("/issueToken", TokenTPResponseDTO.class)))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public Mono<Boolean> verifySignature(Organization organization, Document document, Optional<String> trustedPartyUrl) {
    return DTOFactory.toForm(organization, document)
        .flatMap(trustedPartyUrl
            .map(this::buildWebClient)
            .map(this.client.sendCustomPostRequest("/verifySignature", TokenTPResponseDTO.class))
            .orElse(this.client.sendPostRequest("/verifySignature", TokenTPResponseDTO.class)))
        .then(Mono.just(true));
  }
}
