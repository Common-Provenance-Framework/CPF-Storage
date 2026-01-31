package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationClient;
import org.commonprovenance.framework.store.web.trustedParty.client.TrustedPartyClient;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.OrganizationFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OrganizationClientImpl implements OrganizationClient {
  private final TrustedPartyClient trustedPartyClient;

  public OrganizationClientImpl(
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

  @Override
  public @NotNull Mono<Organization> create(@NotNull String organizationName) {
    return Mono.just(organizationName)
        .flatMap(MONO.makeSureNotNullWithMessage("Organization name can not be null!"))
        .map(OrganizationFormDTO::factory)
        .flatMap(this::postReq)
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Flux<Organization> getAll() {
    return getManyReq()
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Organization> getById(@NotNull UUID id) {
    return Mono.just(id)
        .flatMap(MONO.makeSureNotNullWithMessage("Organization id can not be null!"))
        .map(UUID::toString)
        .flatMap(this::getOneReq)
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Void> deleteById(@NotNull UUID id) {
    return Mono.just(id)
        .flatMap(MONO.makeSureNotNullWithMessage("Organization id can not be null!"))
        .map(UUID::toString)
        .flatMap(this::deleteReq)
        .then();
  }
}
