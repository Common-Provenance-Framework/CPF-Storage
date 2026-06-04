package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Map;
import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.OrganizationFactory;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationWeb;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTrustedParty;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.RegisterOrganizationFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationTPResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrganizationWebImpl implements OrganizationWeb {
  private final String LOG_PREFIX = "OrganizationWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationWebImpl.class);

  private final ClientTrustedParty client;

  public OrganizationWebImpl(
      ClientTrustedParty client) {
    this.client = client;
  }

  @Override
  public Mono<Void> create(Organization organization) {
    return MONO.combineM(
        Mono.just(organization)
            .flatMap(MONO.liftEffectToMono(Organization::getTrustedPartyBaseUrl)),
        Mono.just(organization)
            .flatMap(MONO.liftEffectToMono(RegisterOrganizationFormFactory::build)),
        (optTrustedPartyBaseUrl, form) -> Mono.just(form)
            .flatMap(optTrustedPartyBaseUrl
                .map(this.client.sendCustomPostRequest("/organizations/" + organization.getIdentifier(), Void.class))
                .orElse(this.client.sendPostRequest("/organizations/" + organization.getIdentifier(), Void.class))))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "New organization with identifier '" + organization.getIdentifier() + "' has been registered."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "New organization with identifier '" + organization.getIdentifier() + "' has not been registered!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("New organization with identifier '" + organization.getIdentifier() + "' has not been registered!")));
  }

  @Override
  public Flux<Organization> getAll(Optional<String> optTrustedPartyBaseUrl) {
    return optTrustedPartyBaseUrl
        .map(this.client.sendCustomGetManyRequest("/organizations", OrganizationTPResponseDTO.class, Map.of()))
        .orElse(this.client.sendGetManyRequest("/organizations", OrganizationTPResponseDTO.class, Map.of()))
        .flatMap(MONO.liftEffectToMono(OrganizationFactory::buildUnsafe))
        .doOnComplete(() -> LOGGER.trace(LOG_PREFIX + "Organizations has been fetched."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Organizations has not been fetched!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Organizations has not been fetched!")));
  }

  @Override
  public Mono<Organization> getById(Organization organization) {
    return MONO.combineM(
        MONO.fromEitherOptional(organization.getTrustedPartyBaseUrl()),
        Mono.just(organization).map(Organization::getIdentifier),
        (optTrustedPartyBaseUrl, organizationIdentifier) -> optTrustedPartyBaseUrl
            .map(this.client.sendCustomGetOneRequest("/organizations/" + organizationIdentifier, OrganizationTPResponseDTO.class, Map.of()))
            .orElse(this.client.sendGetOneRequest("/organizations/" + organizationIdentifier, OrganizationTPResponseDTO.class, Map.of())))
        .flatMap(MONO.liftEffectToMono(OrganizationFactory::buildUnsafe))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Organization with identifier '" + organization.getIdentifier() + "' has been fetched."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Organization with identifier '" + organization.getIdentifier() + "' has not been fetched!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Organization with identifier '" + organization.getIdentifier() + "' has not been fetched!")));
  }

}
