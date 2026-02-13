package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationClient;
import org.commonprovenance.framework.store.web.trustedParty.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.DTOFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationTPResponseDTO;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrganizationClientImpl implements OrganizationClient {
  private final Client client;

  public OrganizationClientImpl(
      Client client) {
    this.client = client;
  }

  @Override
  public @NotNull Function<Organization, Mono<Organization>> create(Optional<String> trustedPartyUrl) {
    return (Organization organization) -> Mono.just(organization)
        .flatMap(DTOFactory::toForm)
        .flatMap(trustedPartyUrl
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomPostRequest("/organizations/" + organization.getName(),
                Void.class))
            .orElse(this.client.sendPostRequest("/organizations/" + organization.getName(),
                Void.class)))
        .thenReturn(organization);
  }

  @Override
  public @NotNull Flux<Organization> getAll(Optional<String> trustedPartyUrl) {
    return trustedPartyUrl
        .map(this.client::buildWebClient)
        .map(this.client.sendCustomGetManyRequest("/organizations", OrganizationTPResponseDTO.class))
        .orElse(this.client.sendGetManyRequest("/organizations", OrganizationTPResponseDTO.class))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Function<String, Mono<Organization>> getById(Optional<String> trustedPartyUrl) {
    return (String organizationId) -> MONO.<String>makeSureNotNullWithMessage("Organization id can not be null!")
        .apply(organizationId)
        .flatMap((String id) -> trustedPartyUrl
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomGetOneRequest("/organizations/" + id, OrganizationTPResponseDTO.class))
            .orElse(this.client.sendGetOneRequest("/organizations/" + id, OrganizationTPResponseDTO.class)))
        .flatMap(ModelFactory::toDomain);
  }

}
