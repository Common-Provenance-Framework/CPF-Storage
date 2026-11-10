package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.OrganizationFactory;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationWeb;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.RegisterOrganizationFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.UpdateOrganizationFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.ErrorResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
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
public class OrganizationWebImpl implements OrganizationWeb {
  private final String LOG_PREFIX = "OrganizationWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationWebImpl.class);

  private final Client client;

  public OrganizationWebImpl(
      Client client) {
    this.client = client;
  }

  @Override
  public Mono<Void> create(Organization organization) {
    Function<Organization, String> getUri = org -> "/organizations/" + organization.getIdentifier();

    return MONO.combineM(
        Mono.just(organization)
            .flatMap(MONO.liftEffectToMono(Organization::getTrustedPartyBaseUrl)),
        Mono.just(organization)
            .flatMap(MONO.liftEffectToMono(RegisterOrganizationFormFactory::buildSafe)),
        (optUrl, form) -> Mono.just(form)
            .flatMap(this.client.sendPostRequest(this.client.trustedParty(optUrl), getUri.apply(organization), Void.class, Map.of())))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX
            + "New organization with identifier '"
            + organization.getIdentifier()
            + "' has been registered with Trusted Party at url '"
            + this.getTrustedPartyUrlOrDefault(organization)
            + "'."))
        .doOnError(throwable -> {
          LOGGER.error(LOG_PREFIX
              + "New organization with identifier '"
              + organization.getIdentifier()
              + "' has not been registered with Trusted Party at url '"
              + this.getTrustedPartyUrlOrDefault(organization)
              + "'!\n"
              + throwable.getMessage());
          if (throwable instanceof WebClientResponseException wcrException)
            LOGGER.trace(LOG_PREFIX + wcrException.getResponseBodyAsString());
        })
        .onErrorMap(error -> error instanceof Unauthorized
            || error instanceof BadRequest
            || error instanceof Unauthorized
            || error instanceof Conflict,
            error -> {
              ErrorResponseDTO response = ((WebClientResponseException) error).getResponseBodyAs(ErrorResponseDTO.class);
              return new BadRequestException("TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!");
            })

        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("New organization with identifier '" + organization.getIdentifier() + "' has not been registered!")));
  }

  @Override
  public Mono<Void> updateOrganizationCertificate(Organization organization) {
    Function<Organization, String> getUri = org -> "/organizations/" + organization.getIdentifier() + "/certs";

    return MONO.combineM(
        MONO.fromEitherOptional(organization.getTrustedPartyBaseUrl()),
        Mono.just(organization).flatMap(MONO.liftEffectToMono(UpdateOrganizationFormFactory::buildSafe)),
        (optUrl, form) -> Mono.just(form)
            .flatMap(this.client.sendPutRequest(this.client.trustedParty(optUrl), getUri.apply(organization), Void.class, Map.of())))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Certificates for organization with identifier '" + organization.getIdentifier() + "' has been updated."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Certificates for organization with identifier '" + organization.getIdentifier() + "' has not been updated!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Certificates for organization with identifier '" + organization.getIdentifier() + "' has not been updated!")));
  }

  @Override
  public Mono<Organization> getById(Organization organization) {
    Function<Organization, String> getUri = org -> "/organizations/" + organization.getIdentifier();

    return MONO.fromEitherOptional(organization.getTrustedPartyBaseUrl())
        .flatMap(optUrl -> this.client.sendGetOneRequest(this.client.trustedParty(optUrl), getUri.apply(organization), OrganizationResponseDTO.class, Map.of()))
        .flatMap(MONO.liftEffectToMono(OrganizationFactory::buildUnsafe))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX
            + "Organization with identifier '"
            + organization.getIdentifier()
            + "' has been fetched from Trusted Party at url '"
            + this.getTrustedPartyUrlOrDefault(organization)
            + "'."))
        .doOnError(throwable -> {
          LOGGER.error(LOG_PREFIX
              + "Organization with identifier '"
              + organization.getIdentifier()
              + "' has not been fetched from Trusted Party at url '"
              + this.getTrustedPartyUrlOrDefault(organization)
              + "'!\n"
              + throwable.getMessage());
          if (throwable instanceof WebClientResponseException wcrException)
            LOGGER.trace(LOG_PREFIX + wcrException.getResponseBodyAsString());
        })
        .onErrorMap(NotFound.class, notFound -> {
          ErrorResponseDTO response = notFound.getResponseBodyAs(ErrorResponseDTO.class);
          return new NotFoundException("TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!");
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Organization with identifier '" + organization.getIdentifier() + "' has not been fetched!")));
  }

  private String getTrustedPartyUrlOrDefault(Organization organization) {
    return organization.getTrustedPartyBaseUrl()
        .map(this::getTrustedPartyUrlOrDefault)
        .toOption()
        .get();
  }

  private String getTrustedPartyUrlOrDefault(Optional<String> maybyUrl) {
    return maybyUrl.orElse(this.client.getDefaultTrustedPartyUrl());
  }
}
