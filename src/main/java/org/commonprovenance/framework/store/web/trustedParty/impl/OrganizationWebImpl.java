package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationWeb;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTrustedParty;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.DTOFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationTPResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrganizationWebImpl implements OrganizationWeb {
  private final String LOG_PREFIX = "TrustedPartyWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(TrustedPartyWebImpl.class);

  private final ClientTrustedParty client;

  public OrganizationWebImpl(
      ClientTrustedParty client) {
    this.client = client;
  }

  @Override
  public Function<Organization, Mono<Void>> create(Optional<String> optTrustedPartyUrl) {
    return (Organization organization) -> Mono.just(organization)
        .flatMap(MONO.liftEffectToMono(DTOFactory::toForm))
        .flatMap(optTrustedPartyUrl
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomPostRequest("/organizations/" + organization.getIdentifier(), Void.class))
            .orElse(this.client.sendPostRequest("/organizations/" + organization.getIdentifier(), Void.class)))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "New organization with identifier '" + organization.getIdentifier() + "' has been registered."))
        .doOnError(throwable -> LOGGER.error(
            LOG_PREFIX + "New organization with identifier '" + organization.getIdentifier() + "' has not been registered!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("New organization with identifier '" + organization.getIdentifier() + "' has not been registered!")));
  }

  @Override
  public Flux<Organization> getAll(Optional<String> optTrustedPartyUrl) {
    return optTrustedPartyUrl
        .map(this.client::buildWebClient)
        .map(this.client.sendCustomGetManyRequest("/organizations", OrganizationTPResponseDTO.class, Map.of()))
        .orElse(this.client.sendGetManyRequest("/organizations", OrganizationTPResponseDTO.class, Map.of()))
        .flatMap(MONO.liftEffectToMono(ModelFactory::toDomain))
        .doOnComplete(() -> LOGGER.trace(LOG_PREFIX + "Organizations has been fetched."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Organizations has not been fetched!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Organizations has not been fetched!")));
  }

  @Override
  public Function<String, Mono<Organization>> getById(Optional<String> optTrustedPartyUrl) {
    return (String organizationIdentifier) -> MONO.<String> makeSureNotNullWithMessage("Organization id can not be null!")
        .apply(organizationIdentifier)
        .flatMap((String id) -> optTrustedPartyUrl
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomGetOneRequest("/organizations/" + id, OrganizationTPResponseDTO.class, Map.of()))
            .orElse(this.client.sendGetOneRequest("/organizations/" + id, OrganizationTPResponseDTO.class, Map.of())))
        .flatMap(MONO.liftEffectToMono(ModelFactory::toDomain))
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Organization with identifier '" + organizationIdentifier + "' has been fetched."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Organization with identifier '" + organizationIdentifier + "' has not been fetched!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Organization with identifier '" + organizationIdentifier + "' has not been fetched!")));
  }

}
