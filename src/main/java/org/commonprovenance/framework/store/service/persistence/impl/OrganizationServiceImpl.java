package org.commonprovenance.framework.store.service.persistence.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.UUID;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.persistence.OrganizationPersistence;
import org.commonprovenance.framework.store.service.persistence.OrganizationService;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationPersistence persistence;

  public OrganizationServiceImpl(OrganizationPersistence persistence) {
    this.persistence = persistence;
  }

  @NotNull
  public Mono<Organization> storeOrganization(@NotNull Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be null").apply(organization)
        .flatMap(this.persistence::create);
  }

  @Override
  public @NotNull Mono<Organization> updateOrganization(@NotNull Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be null").apply(organization)
        .flatMap(this.persistence::update);
  }

  @NotNull
  public Mono<Boolean> exists(@NotNull Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be null").apply(organization)
        .map(Organization::getId)
        .flatMap(Mono::justOrEmpty)
        .flatMap(this::getOrganizationById)
        .switchIfEmpty(this.getOrganizationByName(organization.getName()))
        .thenReturn(true)
        .onErrorResume(NotFoundException.class, _ -> Mono.just(false));
  }

  @Override
  public @NotNull Mono<Boolean> notExists(@NotNull Organization organization) {
    return exists(organization).map(result -> !result);
  }

  @NotNull
  public Flux<Organization> getAllOrganizations() {
    return this.persistence.getAll();
  }

  @NotNull
  public Mono<Organization> getOrganizationById(@NotNull UUID id) {
    return this.persistence.getById(id);
  }

  @NotNull
  public Mono<Organization> getOrganizationByName(@NotNull String name) {
    return this.persistence.getByName(name);
  }

  @NotNull
  public Mono<Void> deleteOrganizationById(@NotNull UUID id) {
    return this.persistence.deleteById(id);
  }

}