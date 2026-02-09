package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationClient;
import org.commonprovenance.framework.store.web.trustedParty.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.OrganizationTPFormDTO;
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

  private Mono<OrganizationTPResponseDTO> postReq(OrganizationTPFormDTO body) {
    return client.sendPostRequest("/organizations", OrganizationTPResponseDTO.class)
        .apply(body);
  }

  private Function<String, Mono<OrganizationTPResponseDTO>> getOneReq(String path) {
    return (String value) -> client.sendGetOneRequest(
        "/organizations/" + ((path.isBlank() || path == null) ? "" : "/" + value),
        OrganizationTPResponseDTO.class);
  }

  private Flux<OrganizationTPResponseDTO> getManyReq() {
    return client.sendGetManyRequest("/organizations", OrganizationTPResponseDTO.class);
  }

  private Mono<OrganizationTPResponseDTO> deleteReq(String id) {
    return client.sendDeleteRequest("/organizations/" + id, OrganizationTPResponseDTO.class);
  }

  @Override
  public @NotNull Mono<Organization> create(@NotNull Organization organization) {
    return Mono.just(organization)
        .flatMap(DTOFactory::toForm)
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
    return MONO.<UUID>makeSureNotNullWithMessage("Organization id can not be null!").apply(id)
        .map(UUID::toString)
        .flatMap(this.getOneReq(""))
        .flatMap(ModelFactory::toDomain);
  }

  @Override
  public @NotNull Mono<Organization> getByName(@NotNull String name) {
    return MONO.<String>makeSureNotNullWithMessage("Organization name can not be null!").apply(name)
        .flatMap(this.getOneReq("name"))
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
