package org.commonprovenance.framework.store.service.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.commonprovenance.framework.store.web.trustedParty.CertificateClient;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationClient;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyClient;

import reactor.core.publisher.Mono;

public class TrustedPartyWebServiceImpl implements TrustedPartyWebService {
  private final OrganizationClient organizationClient;
  private final CertificateClient certificateClient;
  private final TrustedPartyClient trustedPartyClient;

  public TrustedPartyWebServiceImpl(
      OrganizationClient organizationClient,
      CertificateClient certificateClient,
      TrustedPartyClient trustedPartyClient) {
    this.organizationClient = organizationClient;
    this.certificateClient = certificateClient;
    this.trustedPartyClient = trustedPartyClient;
  }

  @Override
  public Function<Organization, Mono<Organization>> createOrganization(Optional<String> trustedPartyUri) {
    return (
        Organization organization) -> this.trustedPartyClient.getInfo(trustedPartyUri)
            .flatMap((TrustedParty trustedParty) -> MONO
                .<Organization>makeSureNotNullWithMessage("Organization can not be null!")
                .apply(organization)
                .flatMap(MONO.<Organization>makeSureAsync(this::notExists, "Organization already registered!"))
                .flatMap(this.organizationClient.create(trustedPartyUri))
                .map(org -> org.withTrustedParty(trustedParty)));
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

  @Override
  public Mono<TrustedParty> getTrustedPartyByUrl(Optional<String> trustedPartyUrl) {
    return this.trustedPartyClient.getInfo(trustedPartyUrl);
  }

}
