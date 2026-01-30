package org.commonprovenance.framework.store.web.trustedParty.impl;

import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationsClient;
import org.commonprovenance.framework.store.web.trustedParty.client.TrustedPartyClient;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.mapper.DomainMapper;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrganizationClient implements OrganizationsClient {
  private final TrustedPartyClient trustedPartyClient;

  public OrganizationClient(
      TrustedPartyClient client) {
    this.trustedPartyClient = client;
  }

  private Mono<OrganizationResponseDTO> postReq(OrganizationFormDTO body) {
    return trustedPartyClient.sendPostRequest("/organizations", body, OrganizationResponseDTO.class);
  }

  private Mono<OrganizationResponseDTO> getOneReq(String id) {
    return trustedPartyClient.sendGetOneRequest("/organizations/" + id, OrganizationResponseDTO.class);
  }

  private Flux<OrganizationResponseDTO> getManyReq() {
    return trustedPartyClient.sendGetManyRequest("/organizations", OrganizationResponseDTO.class);
  }

  private Mono<OrganizationResponseDTO> deleteReq(String id) {
    return trustedPartyClient.sendDeleteRequest("/organizations/" + id, OrganizationResponseDTO.class);
  }

  private <T> Mono<T> makeSure(T value, String message) {
    return value == null
        ? Mono.error(new InternalApplicationException(message, new IllegalArgumentException()))
        : Mono.just(value);
  }

  @Override
  public @NotNull Mono<Organization> create(@NotNull String organizationName) {
    return makeSure(organizationName, "Organization name can not be null!")
        .map(OrganizationFormDTO::factory)
        .flatMap(this::postReq)
        .flatMap(DomainMapper::toDomain);
  }

  @Override
  public @NotNull Flux<Organization> getAll() {
    return getManyReq()
        .flatMap(DomainMapper::toDomain);
  }

  @Override
  public @NotNull Mono<Organization> getById(@NotNull UUID id) {
    return makeSure(id, "Organization id can not be null!")
        .map(UUID::toString)
        .flatMap(this::getOneReq)
        .flatMap(DomainMapper::toDomain);
  }

  @Override
  public @NotNull Mono<Void> deleteById(@NotNull UUID id) {
    return makeSure(id, "Organization id can not be null!")
        .map(UUID::toString)
        .flatMap(this::deleteReq)
        .then();
  }
}
