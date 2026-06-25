package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.model.factory.TrustedPartyFactory;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyWeb;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTrustedParty;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.IssueTokenFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.VerifySignatureFormFactory;
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
  public Mono<TrustedParty> getTrustedParty(Optional<String> optTrustedPartyBaseUrl) {
    return optTrustedPartyBaseUrl
        .map(this.client.sendCustomGetOneRequest("/info", TrustedPartyTPResponseDTO.class, Map.of()))
        .orElse(this.client.sendGetOneRequest("/info", TrustedPartyTPResponseDTO.class, Map.of()))
        .flatMap(MONO.liftEffectToMono(TrustedPartyFactory.buildUnsafe(
            this.getTrustedPartyUrl(optTrustedPartyBaseUrl),
            optTrustedPartyBaseUrl.isEmpty())))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Trusted Party info object has been fetched from url: '" + this.getTrustedPartyUrl(optTrustedPartyBaseUrl) + "'."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(
                LOG_PREFIX + "Trusted Party info object has not been fetched from url: '" + this.getTrustedPartyUrl(optTrustedPartyBaseUrl) + "'!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Trusted Party info object has not been fetched from url: '" + this.getTrustedPartyUrl(optTrustedPartyBaseUrl) + "'!")));
  }

  @Override
  public Function<Organization, Mono<Token>> issueGraphToken(String signature) {
    return (Organization organization) -> MONO.fromEither(IssueTokenFormFactory.build(organization, signature))
        .flatMap(organization.getTrustedParty()
            .flatMap(TrustedParty::getUrl)
            .map(this.client.sendCustomPostRequest("/issueToken", TokenTPResponseDTO.class))
            .orElse(this.client.sendPostRequest("/issueToken", TokenTPResponseDTO.class)))
        .flatMap(MONO.liftEffectToMono(TokenFactory::build))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token has been issued by TrustedParty at URL '" + getTrustedPartyUrl(organization) + "'."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Token has not been issued by TrustedParty at URL '" + getTrustedPartyUrl(organization) + "'!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token has not been issued by TrustedParty at URL '" + getTrustedPartyUrl(organization) + "'!")));
  }

  @Override
  public Function<Organization, Mono<Token>> issueGraphToken(GraphType graphType) {
    return (Organization organization) -> MONO.fromEither(IssueTokenFormFactory.build(organization, graphType))
        .flatMap(organization.getTrustedParty()
            .flatMap(TrustedParty::getUrl)
            .map(this.client.sendCustomPostRequest("/issueToken", TokenTPResponseDTO.class))
            .orElse(this.client.sendPostRequest("/issueToken", TokenTPResponseDTO.class)))
        .flatMap(MONO.liftEffectToMono(TokenFactory::build))
        .map(token -> token.withTrustedParty(organization.getTrustedParty()))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Token has been issued by TrustedParty at URL '" + getTrustedPartyUrl(organization) + "'."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Token has not been issued by TrustedParty at URL '" + getTrustedPartyUrl(organization) + "'!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Token has not been issued by TrustedParty at URL '" + getTrustedPartyUrl(organization) + "'!")));
  }

  @Override
  public Function<Organization, Mono<Boolean>> verifySignature(String siganture) {
    return (Organization organization) -> Mono.just(organization)
        .flatMap(MONO.liftEffectToMono(VerifySignatureFormFactory.build(siganture)))
        .flatMap(organization.getTrustedParty()
            .flatMap(TrustedParty::getUrl)
            .map(this.client.sendCustomPostRequest("/verifySignature", Void.class))
            .orElse(this.client.sendPostRequest("/verifySignature", Void.class)))
        .thenReturn(true)
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

  private String getTrustedPartyUrl(Optional<String> optTrustedPartyBaseUrl) {
    return optTrustedPartyBaseUrl.orElse(this.client.getDefaultTrustedPartyUrl());
  }

  private String getTrustedPartyUrl(Organization organization) {
    return organization.getTrustedParty()
        .flatMap(TrustedParty::getUrl)
        .orElse(this.client.getDefaultTrustedPartyUrl());
  }

}
