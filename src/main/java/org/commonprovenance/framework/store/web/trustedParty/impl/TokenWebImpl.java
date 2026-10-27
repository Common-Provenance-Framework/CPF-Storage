package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.web.trustedParty.TokenWeb;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.ErrorResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenResponseDTO;
import org.openprovenance.prov.model.QualifiedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

import reactor.core.publisher.Flux;

@Component
public class TokenWebImpl implements TokenWeb {
  private final String LOG_PREFIX = "TokenWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenWebImpl.class);

  private final Client client;

  public TokenWebImpl(
      Client client) {
    this.client = client;
  }

  private String getTokensUri(String id) {
    return "/organizations/" + id + "/tokens";
  }

  @Override
  public Function<String, Flux<Token>> getAllByOrganization(Optional<String> optTrustedPartyBaseUrl) {
    return (String organizationIdentifier) -> this.client
        .sendGetManyRequest(this.client.trustedParty(optTrustedPartyBaseUrl), getTokensUri(organizationIdentifier), TokenResponseDTO.class, Map.of())
        .flatMap(MONO.liftEffectToMono(TokenFactory::build))
        .doOnComplete(() -> LOGGER.trace(LOG_PREFIX
            + "Tokens has been fetched from TrustedParty at URL '"
            + getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl)
            + "'."))
        .doOnError(throwable -> {
          LOGGER.error(LOG_PREFIX
              + "Tokens has not been fetched from TrustedParty at URL '"
              + getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl) + "'!\n"
              + throwable.getMessage());
          if (throwable instanceof WebClientResponseException wcrException)
            LOGGER.trace(LOG_PREFIX + wcrException.getResponseBodyAsString());
        })
        .onErrorMap(NotFound.class, notFound -> {
          ErrorResponseDTO response = notFound.getResponseBodyAs(ErrorResponseDTO.class);
          return new NotFoundException("TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!");
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Tokens for organization with id '" + organizationIdentifier + "' has not been fetched!")));
  }

  @Override
  public Flux<Token> getByDocumentId(
      String organizationIdentifier,
      QualifiedName bundleIdentifier,
      GraphFormat graphFormat,
      Optional<String> optTrustedPartyBaseUrl) {
    String uri = getTokensUri(organizationIdentifier) + "/" + bundleIdentifier.getLocalPart() + "/" + graphFormat.toString();

    return this.client
        .sendGetManyRequest(this.client.trustedParty(optTrustedPartyBaseUrl), uri, TokenResponseDTO.class, Map.of())
        .flatMap(MONO.liftEffectToMono(TokenFactory::build))
        .doOnComplete(() -> LOGGER.trace(LOG_PREFIX
            + "Token has been fetched from TrustedParty at URL '"
            + getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl)
            + "'."))
        .doOnError(throwable -> {
          LOGGER.error(LOG_PREFIX
              + "Token has not been fetched from TrustedParty at URL '"
              + getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl) + "'!\n"
              + throwable.getMessage());
          if (throwable instanceof WebClientResponseException wcrException)
            LOGGER.trace(LOG_PREFIX + wcrException.getResponseBodyAsString());
        })
        .onErrorMap(NotFound.class, notFound -> {
          ErrorResponseDTO response = notFound.getResponseBodyAs(ErrorResponseDTO.class);
          return new NotFoundException("TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!");
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Token has not been fetched!")));
  }

  private String getTrustedPartyUrlOrDefault(Optional<String> optTrustedPartyBaseUrl) {
    return optTrustedPartyBaseUrl.orElse(this.client.getDefaultTrustedPartyUrl());
  }
}
