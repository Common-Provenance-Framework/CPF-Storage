package org.commonprovenance.framework.store.web.trustedParty.impl;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyClient;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTP;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.DTOFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TrustedPartyTPResponseDTO;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class TrustedPartyClientImpl implements TrustedPartyClient {
  private final ClientTP client;

  public TrustedPartyClientImpl(
      ClientTP client) {
    this.client = client;
  }

  @Override
  public Mono<TrustedParty> getInfo(Optional<String> trustedPartyUrl) {
    return Mono.justOrEmpty(trustedPartyUrl)
        .map(this.client::buildWebClient)
        .flatMap(this.client.sendCustomGetOneRequest("/info", TrustedPartyTPResponseDTO.class))
        .switchIfEmpty(this.client.sendGetOneRequest("/info", TrustedPartyTPResponseDTO.class))
        .flatMap(ModelFactory.toDomain(
            trustedPartyUrl.orElse(this.client.getUrl()),
            trustedPartyUrl.isPresent() ? false : true));
  }

  @Override
  public Function<Document, Mono<Token>> issueGraphToken(Optional<String> trustedPartyUrl) {
    return (Document document) -> DTOFactory.toForm(document, GraphType.GRAPH)
        .flatMap(trustedPartyUrl
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomPostRequest("/issueToken", TokenTPResponseDTO.class))
            .orElse(this.client.sendPostRequest("/issueToken", TokenTPResponseDTO.class)))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public Function<Document, Mono<Boolean>> verifySignature(Organization organization) {
    return (Document document) -> DTOFactory.toForm(organization, document)
        .flatMap(organization.getTrustedParty().getUrl()
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomPostRequest("/verifySignature", Void.class))
            .orElse(this.client.sendPostRequest("/verifySignature", Void.class)))
        .then(Mono.just(true))
        .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest.class,
            ex -> Mono.just(false));
  }
}
