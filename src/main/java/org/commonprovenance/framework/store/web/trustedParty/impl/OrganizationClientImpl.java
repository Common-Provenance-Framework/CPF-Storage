package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;

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
  public @NotNull Mono<Organization> create(
      @NotNull Organization organization,
      Optional<String> trustedPartyUrl) {
    return Mono.just(organization)
        .flatMap(DTOFactory::toForm)
        .flatMap(trustedPartyUrl
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomPostRequest("/organizations", OrganizationTPResponseDTO.class))
            .orElse(this.client.sendPostRequest("/organizations", OrganizationTPResponseDTO.class)))
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
  public @NotNull Mono<Organization> getById(
      @NotNull String organizationId,
      Optional<String> trustedPartyUrl) {
    return MONO.<String>makeSureNotNullWithMessage("Organization id can not be null!").apply(organizationId)
        .flatMap((String id) -> trustedPartyUrl
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomGetOneRequest("/organizations/" + id, OrganizationTPResponseDTO.class))
            .orElse(this.client.sendGetOneRequest("/organizations/" + id, OrganizationTPResponseDTO.class)))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Void> deleteById(
      @NotNull String organizationId,
      Optional<String> trustedPartyUrl) {
    return Mono.just(organizationId)
        .flatMap((String id) -> trustedPartyUrl
            .map(this.client::buildWebClient)
            .map(this.client.sendCustomDeleteRequest("/organizations/" + id, OrganizationTPResponseDTO.class))
            .orElse(this.client.sendDeleteRequest("/organizations/" + id, OrganizationTPResponseDTO.class)))
        .flatMap(MONO.makeSureNotNullWithMessage("Organization id can not be null!"))
        .then();
  }
}
