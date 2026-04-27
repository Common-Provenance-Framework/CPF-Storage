package org.commonprovenance.framework.store.service.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.OrganizationService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TrustedPartyService;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.commonprovenance.framework.store.web.trustedParty.CertificateClient;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationClient;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyClient;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class TrustedPartyWebServiceImpl implements TrustedPartyWebService {
  private final OrganizationClient organizationClient;
  private final CertificateClient certificateClient;
  private final TrustedPartyClient trustedPartyClient;

  private final TrustedPartyService trustedPartyService;

  public TrustedPartyWebServiceImpl(
      OrganizationClient organizationClient,
      CertificateClient certificateClient,
      TrustedPartyClient trustedPartyClient,
      TrustedPartyService trustedPartyService) {
    this.organizationClient = organizationClient;
    this.certificateClient = certificateClient;
    this.trustedPartyClient = trustedPartyClient;

    this.trustedPartyService = trustedPartyService;
  }

  @Override
  public Function<Organization, Mono<Organization>> createOrganization(Optional<String> trustedPartyUri) {
    return (
        Organization organization) -> this.trustedPartyClient.getInfo(trustedPartyUri)
            .flatMap((TrustedParty trustedParty) -> MONO
                .<Organization> makeSureNotNullWithMessage("Organization can not be null!")
                .apply(organization)
                .flatMap(MONO.<Organization> makeSureAsync(this::notExists, "Organization already registered!"))
                .flatMap(this.organizationClient.create(trustedPartyUri))
                .map(org -> org.withTrustedParty(trustedParty)));
  }

  @Override
  public Mono<Organization> updateOrganization(Organization organization) {
    return MONO.<Organization> makeSureNotNullWithMessage("Organization can not be null!").apply(organization)
        .flatMap(MONO.<Organization> makeSureAsync(this::exists, "Organization does not registered!"))
        .flatMap(this.certificateClient.updateOrganizationCertificate(Optional.empty()));
  }

  @Override
  public Mono<Boolean> exists(Organization organization) {
    return MONO.<Organization> makeSureNotNullWithMessage("Organization can not be null!").apply(organization)
        .map(Organization::getIdentifier)
        .flatMap(Mono::justOrEmpty)
        .flatMap(this.organizationClient.getById(Optional.empty()))
        .hasElement()
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

  @Override
  public Function<Organization, Mono<Void>> verifySignature(Document document) {
    return (Organization organization) -> MONO.<Document> makeSureNotNullWithMessage("Document can not be null!")
        .apply(document)
        .flatMap(MONO.makeSureAsync(
            this.trustedPartyClient.verifySignature(organization),
            _ -> new BadRequestException("Invalid signature!")))
        .then();

  }

  @Override
  public Function<Document, Mono<Token>> issueGraphToken(Optional<String> trustedPartyUrl) {
    return this.trustedPartyClient.issueGraphToken(trustedPartyUrl, GraphType.GRAPH);
  }

  @Override
  public Mono<Token> issueGraphToken(Document document) {
    return MONO.<Document> makeSureNotNullWithMessage("Document can not be null!")
        .apply(document)
        .map(Document::getOrganizationIdentifier)
        .flatMap(this.trustedPartyService::getTrustedPartyByOrganizationIdentifier)
        .flatMap(trustedParty -> this.issueBackboneGraphToken(trustedParty.getUrlIfNotDefault())
            .apply(document)
            .map(token -> token
                .withDocument(document)
                .withTrustedParty(trustedParty)));
  }

  @Override
  public Function<Document, Mono<Token>> issueDomainSpecificGraphToken(Optional<String> trustedPartyUrl) {
    return this.trustedPartyClient.issueGraphToken(trustedPartyUrl, GraphType.DOMAIN_SPECIFIC);
  }

  @Override
  public Function<Document, Mono<Token>> issueBackboneGraphToken(Optional<String> trustedPartyUrl) {
    return this.trustedPartyClient.issueGraphToken(trustedPartyUrl, GraphType.BACKBONE);
  }

}
