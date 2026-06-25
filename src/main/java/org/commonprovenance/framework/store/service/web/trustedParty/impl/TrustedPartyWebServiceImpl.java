package org.commonprovenance.framework.store.service.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.TrustedPartyService;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.commonprovenance.framework.store.web.trustedParty.CertificateWeb;
import org.commonprovenance.framework.store.web.trustedParty.OrganizationWeb;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyWeb;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class TrustedPartyWebServiceImpl implements TrustedPartyWebService {
  private final OrganizationWeb organizationWeb;
  private final CertificateWeb certificateWeb;
  private final TrustedPartyWeb trustedPartyWeb;

  private final TrustedPartyService trustedPartyService;

  public TrustedPartyWebServiceImpl(
      OrganizationWeb organizationWeb,
      CertificateWeb certificateWeb,
      TrustedPartyWeb trustedPartyWeb,
      TrustedPartyService trustedPartyService) {
    this.organizationWeb = organizationWeb;
    this.certificateWeb = certificateWeb;
    this.trustedPartyWeb = trustedPartyWeb;

    this.trustedPartyService = trustedPartyService;
  }

  @Override
  public Mono<Void> registerOrganization(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.<Organization> makeSureAsync(
            this::organizationIsNotRegistered,
            org -> new ConflictException("Organization with identifier '" + organization.getIdentifier() + "' already registered in TrustedParty!")))
        .flatMap(this.organizationWeb::create);
  }

  @Override
  public Mono<Void> updateOrganization(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.makeSureAsync(
            this::organizationIsRegistered,
            org -> new BadRequestException("Organization with identifier '" + org.getIdentifier() + "' has not been registered yet!")))
        .flatMap(this.certificateWeb::updateOrganizationCertificate);
  }

  @Override
  public Mono<Boolean> organizationIsRegistered(Organization organization) {
    return Mono.just(organization)
        .flatMap(this.organizationWeb::getById)
        .hasElement()
        .onErrorResume(NotFoundException.class, _ -> Mono.just(false));
  }

  @Override
  public Mono<Boolean> organizationIsNotRegistered(Organization organization) {
    return Mono.just(organization)
        .flatMap(this::organizationIsRegistered)
        .map(exists -> !exists);
  }

  @Override
  public Function<Organization, Mono<Organization>> setTrustedPartyByBaseUrl(Optional<String> optTrustedPartyBaseUrl) {
    return (Organization organization) -> this.trustedPartyWeb.getTrustedParty(optTrustedPartyBaseUrl)
        .onErrorMap(ApplicationException.class, appException -> optTrustedPartyBaseUrl
            .<ApplicationException> map(tpBaseURL -> new BadRequestException("TrustedParty is not reachable at '" + tpBaseURL + "'!", appException))
            .orElseGet(() -> appException))
        .map((TrustedParty trustedParty) -> organization.withTrustedParty(trustedParty));
  }

  @Override
  public Function<Organization, Mono<Void>> verifySignature(String signature) {
    return (Organization organization) -> Mono.just(organization)
        .flatMap(MONO.makeSureAsync(
            this.trustedPartyWeb.verifySignature(signature),
            _ -> new BadRequestException("Invalid signature!")))
        .then();

  }

  @Override
  public Function<Organization, Mono<Organization>> issueGraphToken(String signature) {
    return (Organization organization) -> Mono.just(organization)
        .flatMap(this.trustedPartyWeb.issueGraphToken(signature))
        .flatMap(MONO.liftOptionalToMono(
            token -> organization.getDocument().map(document -> document.withToken(token)),
            _ -> new InvalidValueException("Document has not been deserialized yet!")))
        .map(organization::withDocument);
  }

  @Override
  public Mono<Organization> issueDomainSpecificGraphToken(Organization organization) {
    return Mono.just(organization)
        .flatMap(this.trustedPartyWeb.issueGraphToken(GraphType.DOMAIN_SPECIFIC))
        .flatMap(MONO.liftOptionalToMono(
            token -> organization.getDocument().map(document -> document.withToken(token)),
            _ -> new InvalidValueException("Document has not been deserialized yet!")))
        .map(organization::withDocument);
  }

  @Override
  public Mono<Organization> issueBackboneGraphToken(Organization organization) {
    return Mono.just(organization)
        .flatMap(this.trustedPartyWeb.issueGraphToken(GraphType.BACKBONE))
        .flatMap(MONO.liftOptionalToMono(
            token -> organization.getDocument().map(document -> document.withToken(token)),
            _ -> new InvalidValueException("Document has not been deserialized yet!")))
        .map(organization::withDocument);
  }

}
