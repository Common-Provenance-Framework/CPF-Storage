package org.commonprovenance.framework.store.service.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyService;
import org.commonprovenance.framework.store.web.trustedParty.CertificateClient;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationClient;

import reactor.core.publisher.Mono;

public class TrustedPartyServiceImpl implements TrustedPartyService {
  private final OrganizationClient organizationClient;
  private final CertificateClient certificateClient;

  public TrustedPartyServiceImpl(
      OrganizationClient organizationClient,
      CertificateClient certificateClient) {
    this.organizationClient = organizationClient;
    this.certificateClient = certificateClient;
  }

  @Override
  public Mono<Organization> createOrganization(Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be null!").apply(organization)
        .flatMap(MONO.<Organization>makeSureAsync(this::notExists, "Organization already registered!"))
        .flatMap(this.organizationClient.create(Optional.empty()));
  }

  @Override
  public Mono<Organization> updateOrganization(Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be null!").apply(organization)
        .flatMap(MONO.<Organization>makeSureAsync(this::exists, "Organization does not registered!"))
        .flatMap(this.certificateClient.updateOrganizationCertificate(Optional.empty()));
  }

  @Override
  public Mono<Boolean> exists(Organization organization) {
    return MONO.<Organization>makeSureNotNullWithMessage("Organization can not be null!").apply(organization)
        .map(Organization::getName)
        .flatMap(Mono::justOrEmpty)
        .flatMap(this.organizationClient.getById(Optional.empty()))
        .thenReturn(true)
        .switchIfEmpty(Mono.just(false))
        .onErrorResume(NotFoundException.class, _ -> Mono.just(false));
  }

  @Override
  public Mono<Boolean> notExists(Organization organization) {
    return this.exists(organization).map(exists -> !exists);
  }
}
