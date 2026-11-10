package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.CertificateWeb;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.UpdateOrganizationFormFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
}
