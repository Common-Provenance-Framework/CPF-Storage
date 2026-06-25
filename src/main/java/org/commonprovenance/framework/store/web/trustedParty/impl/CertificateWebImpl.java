package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.filter.AccessLogFilter;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.OrganizationFactory;
import org.commonprovenance.framework.store.web.trustedParty.CertificateWeb;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTrustedParty;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.UpdateOrganizationFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.CertificateTPResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class CertificateWebImpl implements CertificateWeb {
  private final AccessLogFilter accessLogFilter;
  private final String LOG_PREFIX = "CertificateWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(CertificateWebImpl.class);

  private final ClientTrustedParty client;

  public CertificateWebImpl(
      ClientTrustedParty client, AccessLogFilter accessLogFilter) {
    this.client = client;
    this.accessLogFilter = accessLogFilter;
  }

  private String getUri(String id) {
    return "/organizations/" + id + "/certs";
  }

  private String getUri(Organization organization) {
    return getUri(organization.getIdentifier());
  }

  @Override
  public Function<String, Mono<Organization>> getOrganizationCertificate(Optional<String> optTrustedPartyBaseUrl) {
    return (String organizationIdentifier) -> optTrustedPartyBaseUrl
        .map(this.client.sendCustomGetOneRequest(getUri(organizationIdentifier), CertificateTPResponseDTO.class, Map.of()))
        .orElse(this.client.sendGetOneRequest(getUri(organizationIdentifier), CertificateTPResponseDTO.class, Map.of()))
        .flatMap(MONO.liftEffectToMono(OrganizationFactory::buildUnsafe))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Certificates for Organization with identifier '" + organizationIdentifier + "' has been fetched."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Certificates for Organization with identifier '" + organizationIdentifier + "' has not been fetched!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Certificates for Organization with identifier '" + organizationIdentifier + "' has not been fetched!")));
  }

  @Override
  public Mono<Void> updateOrganizationCertificate(Organization organization) {
    return MONO.combineM(
        MONO.fromEitherOptional(organization.getTrustedPartyBaseUrl()),
        Mono.just(organization).flatMap(MONO.liftEffectToMono(UpdateOrganizationFormFactory::build)),
        (optTrustedPartyBaseUrl, form) -> Mono.just(form)
            .flatMap(optTrustedPartyBaseUrl
                .map(this.client.sendCustomPutRequest(getUri(organization), Void.class))
                .orElse(this.client.sendPutRequest(getUri(organization), Void.class))))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Certificates for organization with identifier '" + organization.getIdentifier() + "' has been updated."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "Certificates for organization with identifier '" + organization.getIdentifier() + "' has not been updated!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Certificates for organization with identifier '" + organization.getIdentifier() + "' has not been updated!")));
  }

}
