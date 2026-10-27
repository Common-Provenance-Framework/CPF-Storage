package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.MetaDocument;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.model.factory.TrustedPartyFactory;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyWeb;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.IssueTokenFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.VerifySignatureFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.ErrorResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TrustedPartyResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException.Conflict;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized;

import reactor.core.publisher.Mono;

@Component
public class TrustedPartyWebImpl implements TrustedPartyWeb {
  private final String LOG_PREFIX = "TrustedPartyWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(TrustedPartyWebImpl.class);

  private final Client client;

  public TrustedPartyWebImpl(
      Client client) {
    this.client = client;
  }

  @Override
  public Mono<TrustedParty> getTrustedParty(Optional<String> optTrustedPartyBaseUrl) {
    return this.client
        .sendGetOneRequest(this.client.trustedParty(optTrustedPartyBaseUrl), "/info", TrustedPartyResponseDTO.class, Map.of())
        .flatMap(MONO.liftEffectToMono(TrustedPartyFactory.buildUnsafe(
            this.getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl),
            optTrustedPartyBaseUrl.isEmpty())))
        .onErrorResume(NotFound.class, notFound -> {
          ErrorResponseDTO response = notFound.getResponseBodyAs(ErrorResponseDTO.class);
          return Mono.error(
              () -> new NotFoundException("Resource not found! TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!"));
        })
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Trusted Party info object has been fetched from url: '" + this.getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl) + "'."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(
                LOG_PREFIX + "Trusted Party info object has not been fetched from url: '" + this.getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl) + "'!\n"
                    + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Trusted Party info object has not been fetched from url: '" + this.getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl) + "'!")));
  }

  @Override
  public Function<Organization, Mono<Token>> issueGraphToken(String signature) {
    return (Organization organization) -> MONO.fromEither(IssueTokenFormFactory.buildSafe(organization, signature))
        .flatMap(this.client.sendPostRequest(
            this.client.trustedParty(getTrustedPartyUrl(organization)), "/issueToken", TokenResponseDTO.class, Map.of()))
        .flatMap(MONO.liftEffectToMono(TokenFactory::build))
        .onErrorResume(error -> error instanceof Unauthorized
            || error instanceof BadRequest
            || error instanceof NotFound
            || error instanceof Conflict,
            error -> {
              ErrorResponseDTO response = ((WebClientResponseException) error).getResponseBodyAs(ErrorResponseDTO.class);
              return Mono.error(() -> new BadRequestException("TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!"));
            })
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token has been issued by TrustedParty at URL '" + getTrustedPartyUrlOrDefault(organization) + "'."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Token has not been issued by TrustedParty at URL '" + getTrustedPartyUrlOrDefault(organization) + "'!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token has not been issued by TrustedParty at URL '" + getTrustedPartyUrlOrDefault(organization) + "'!")));
  }

  @Override
  public Function<Organization, Mono<Token>> issueGraphToken(GraphType graphType) {
    return (Organization organization) -> MONO.fromEither(IssueTokenFormFactory.buildSafe(organization, graphType))
        .flatMap(this.client.sendPostRequest(
            this.client.trustedParty(getTrustedPartyUrl(organization)), "/issueToken", TokenResponseDTO.class, Map.of()))
        .flatMap(MONO.liftEffectToMono(TokenFactory::build))
        .map(token -> token.withTrustedParty(organization.getTrustedParty()))
        .onErrorResume(error -> error instanceof Unauthorized
            || error instanceof BadRequest
            || error instanceof NotFound
            || error instanceof Conflict,
            error -> {
              ErrorResponseDTO response = ((WebClientResponseException) error).getResponseBodyAs(ErrorResponseDTO.class);
              return Mono.error(() -> new BadRequestException("TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!"));
            })
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token has been issued by TrustedParty at URL '" + getTrustedPartyUrlOrDefault(organization) + "'."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Token has not been issued by TrustedParty at URL '" + getTrustedPartyUrlOrDefault(organization) + "'!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token has not been issued by TrustedParty at URL '" + getTrustedPartyUrlOrDefault(organization) + "'!")));
  }

  @Override
  public Mono<Token> issueGraphToken(MetaDocument metaDocument) {
    return Mono.justOrEmpty(metaDocument)
        .flatMap(MONO.liftEffectToMono(IssueTokenFormFactory::buildSafe))
        .flatMap(this.client.sendPostRequest(
            this.client.trustedParty(getTrustedPartyUrl(metaDocument)),
            "/issueToken",
            TokenResponseDTO.class,
            Map.of()))
        .flatMap(MONO.liftEffectToMono(TokenFactory::build))
        .onErrorResume(error -> error instanceof Unauthorized
            || error instanceof BadRequest
            || error instanceof NotFound
            || error instanceof Conflict,
            error -> {
              ErrorResponseDTO response = ((WebClientResponseException) error).getResponseBodyAs(ErrorResponseDTO.class);
              return Mono.error(() -> new BadRequestException("TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!"));
            })
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token has been issued by TrustedParty."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Token has not been issued by TrustedParty!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token has not been issued by TrustedParty!")));

  }

  @Override
  public Function<Organization, Mono<Boolean>> verifySignature(String siganture) {
    return (Organization organization) -> Mono.just(organization)
        .flatMap(MONO.liftEffectToMono(VerifySignatureFormFactory.buildSafe(siganture)))
        .flatMap(this.client.sendPostRequest(
            this.client.trustedParty(getTrustedPartyUrl(organization)), "/verifySignature", Boolean.class, Map.of()))
        .thenReturn(true)
        .onErrorResume(BadRequest.class, badRequest -> {
          ErrorResponseDTO response = badRequest.getResponseBodyAs(ErrorResponseDTO.class);
          return Mono.error(() -> new BadRequestException("TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!"));
        })
        .onErrorResume(NotFound.class, notFound -> {
          ErrorResponseDTO response = notFound.getResponseBodyAs(ErrorResponseDTO.class);
          return Mono.error(() -> new BadRequestException("TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!"));
        })
        .onErrorResume(Unauthorized.class, unauthorized -> {
          return Mono.just(false);
        })
        .doOnSuccess(valid -> {
          if (valid)
            LOGGER.trace(LOG_PREFIX + "Signature has been verified by TrustedParty at URL '" + getTrustedPartyUrlOrDefault(organization) + "' and is valid.");
          else
            LOGGER.trace(LOG_PREFIX + "Signature has been verified by TrustedParty at URL '" + getTrustedPartyUrlOrDefault(organization) + "' and is not valid.");
        })
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Signature has not been verified by TrustedParty at URL '" + getTrustedPartyUrlOrDefault(organization) + "'!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Signature has not been verified by TrustedParty at URL '" + getTrustedPartyUrlOrDefault(organization) + "'!")));
  }

  private Optional<String> getTrustedPartyUrl(MetaDocument metaDocument) {
    return metaDocument.getTrustedParty(client.getDefaultTrustedPartyUrl()).getUrl()
        .map(url -> url.startsWith("http") ? url : "http://" + url + "/api/v1/");
  }

  private Optional<String> getTrustedPartyUrl(Organization organization) {
    return organization.getTrustedParty()
        .flatMap(TrustedParty::getUrl);
  }

  private String getTrustedPartyUrlOrDefault(Optional<String> optTrustedPartyBaseUrl) {
    return optTrustedPartyBaseUrl.orElse(this.client.getDefaultTrustedPartyUrl());
  }

  private String getTrustedPartyUrlOrDefault(Organization organization) {
    return getTrustedPartyUrl(organization)
        .orElse(this.client.getDefaultTrustedPartyUrl());
  }

}
