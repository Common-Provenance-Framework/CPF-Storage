package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyWeb;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTrustedParty;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.DTOFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TrustedPartyTPResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;

import reactor.core.publisher.Mono;

@Component
public class TrustedPartyWebImpl implements TrustedPartyWeb {
  private final String LOG_PREFIX = "TrustedPartyWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(TrustedPartyWebImpl.class);

  private final ClientTrustedParty client;

  public TrustedPartyWebImpl(
      ClientTrustedParty client) {
    this.client = client;
  }

  @Override
  public Mono<TrustedParty> getInfo(Optional<String> optTrustedPartyUrl) {
    return optTrustedPartyUrl
        .map(this.client.sendCustomGetOneRequest("/info", TrustedPartyTPResponseDTO.class, Map.of()))
        .orElse(this.client.sendGetOneRequest("/info", TrustedPartyTPResponseDTO.class, Map.of()))
        .flatMap(MONO.liftEffectToMono(ModelFactory.toDomain(
            this.getTrustedPartyUrl(optTrustedPartyUrl),
            optTrustedPartyUrl.isEmpty())))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Trusted Party info object has been fetched from url: '" + this.getTrustedPartyUrl(optTrustedPartyUrl) + "'."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Trusted Party info object has not been fetched from url: '" + this.getTrustedPartyUrl(optTrustedPartyUrl) + "'!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Trusted Party info object has not been fetched from url: '" + this.getTrustedPartyUrl(optTrustedPartyUrl) + "'!")));
  }

  @Override
  public Function<Document, Mono<Token>> issueGraphToken(Optional<String> optTrustedPartyUrl, GraphType graphType) {
    return (Document document) -> MONO.fromEither(DTOFactory.toForm(document, graphType))
        .flatMap(optTrustedPartyUrl
            .map(this.client.sendCustomPostRequest("/issueToken", TokenTPResponseDTO.class))
            .orElse(this.client.sendPostRequest("/issueToken", TokenTPResponseDTO.class)))
        .flatMap(MONO.liftEffectToMono(ModelFactory::toDomain))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token has been issued by TrustedParty at URL '" + getTrustedPartyUrl(optTrustedPartyUrl) + "'."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Token has not been issued by TrustedParty at URL '" + getTrustedPartyUrl(optTrustedPartyUrl) + "'!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token has not been issued by TrustedParty at URL '" + getTrustedPartyUrl(optTrustedPartyUrl) + "'!")));
  }

  @Override
  public Function<Document, Mono<Boolean>> verifySignature(Organization organization) {
    return (Document document) -> MONO.fromEither(DTOFactory.toForm(organization, document))
        .flatMap(organization.getTrustedParty()
            .flatMap(TrustedParty::getUrl)
            .map(this.client.sendCustomPostRequest("/verifySignature", Void.class))
            .orElse(this.client.sendPostRequest("/verifySignature", Void.class)))
        .then(Mono.just(true))
        .onErrorResume(BadRequest.class, _ -> Mono.just(false))
        .doOnSuccess(valid -> {
          if (valid)
            LOGGER.trace(LOG_PREFIX + "Signature has been verified by TrustedParty at URL '" + getTrustedPartyUrl(organization) + "' and is valid.");
          else
            LOGGER.trace(LOG_PREFIX + "Signature has been verified by TrustedParty at URL '" + getTrustedPartyUrl(organization) + "' and is not valid.");
        })
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Signature has not been verified by TrustedParty at URL '" + getTrustedPartyUrl(organization) + "'!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Signature has not been verified by TrustedParty at URL '" + getTrustedPartyUrl(organization) + "'!")));
  }

  private String getTrustedPartyUrl(Optional<String> optTrustedPartyUrl) {
    return optTrustedPartyUrl.orElse(this.client.getDefaultTrustedPartyUrl());
  }

  private String getTrustedPartyUrl(Organization organization) {
    return organization.getTrustedParty()
        .flatMap(TrustedParty::getUrl)
        .orElse(this.client.getDefaultTrustedPartyUrl());
  }

}
