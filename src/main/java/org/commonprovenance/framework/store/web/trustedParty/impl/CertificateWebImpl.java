package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.OrganizationFactory;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.CertificateWeb;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.UpdateOrganizationFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.ErrorResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

import reactor.core.publisher.Mono;

@Component
public class CertificateWebImpl implements CertificateWeb {
  private final String LOG_PREFIX = "CertificateWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(CertificateWebImpl.class);

  private final Client client;

  public CertificateWebImpl(
      Client client) {
    this.client = client;
  }

  private String getUri(String id) {
    return "/organizations/" + id + "/certs";
  }

  private String getUri(Organization organization) {
    return getUri(organization.getIdentifier());
  }

  @Override
  public Function<String, Mono<Organization>> getOrganizationCertificate(Optional<String> optTrustedPartyBaseUrl) {
    return (String organizationIdentifier) -> this.client
        .sendGetOneRequest(this.client.trustedParty(optTrustedPartyBaseUrl), getUri(organizationIdentifier), OrganizationResponseDTO.class, Map.of())
        .flatMap(MONO.liftEffectToMono(OrganizationFactory::buildUnsafe))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX
            + "Certificates for Organization with identifier '"
            + organizationIdentifier
            + "' has been fetched from Trusted Party at url '"
            + this.getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl)
            + "'."))
        .doOnError(throwable -> {
          LOGGER.error(LOG_PREFIX
              + "Certificates for Organization with identifier '"
              + organizationIdentifier
              + "' has not been fetched from Trusted Party at url '"
              + this.getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl)
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
            new InternalApplicationException("Certificates for Organization with identifier '" + organizationIdentifier + "' has not been fetched!")));
  }

  @Override
  public Mono<Void> updateOrganizationCertificate(Organization organization) {
    return MONO.combineM(
        MONO.fromEitherOptional(organization.getTrustedPartyBaseUrl()),
        Mono.just(organization).flatMap(MONO.liftEffectToMono(UpdateOrganizationFormFactory::buildSafe)),
        (optTrustedPartyBaseUrl, form) -> Mono.just(form)
            .flatMap(this.client.sendPutRequest(this.client.trustedParty(optTrustedPartyBaseUrl), getUri(organization), Void.class, Map.of())))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Certificates for organization with identifier '" + organization.getIdentifier() + "' has been updated."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Certificates for organization with identifier '" + organization.getIdentifier() + "' has not been updated!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Certificates for organization with identifier '" + organization.getIdentifier() + "' has not been updated!")));
  }

  private String getTrustedPartyUrlOrDefault(Optional<String> maybyUrl) {
    return maybyUrl.orElse(this.client.getDefaultTrustedPartyUrl());
  }
}
