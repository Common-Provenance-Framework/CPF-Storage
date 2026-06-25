package org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.OrganizationRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TrustedPartyRepository;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.OrganizationService;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class OrganizationServiceImpl implements OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final TrustedPartyRepository trustedPartyRepository;
  private final DocumentRepository documentRepository;
  private final TokenRepository tokenRepository;

  public OrganizationServiceImpl(
      OrganizationRepository organizationRepository,
      TrustedPartyRepository trustedPartyRepository,
      DocumentRepository documentRepository,
      TokenRepository tokenRepository) {
    this.organizationRepository = organizationRepository;
    this.trustedPartyRepository = trustedPartyRepository;
    this.documentRepository = documentRepository;
    this.tokenRepository = tokenRepository;
  }

  @Override
  public Mono<Void> storeOrganization(Organization organization) {
    return Mono.just(organization)
        .delayUntil(MONO.makeSureAsync(
            this::notExists,
            org -> new ConflictException("Organization with identifier '" + org.getIdentifier() + "' already registered in CPF-Store!")))
        .delayUntil(this.organizationRepository::save)
        .delayUntil(this.organizationRepository::connectTrusts)
        .then();
  }

  @Override
  public Mono<Void> updateOrganization(Organization organization) {
    return Mono.just(organization)
        .flatMap((MONO.makeSureAsync(
            this::exists,
            org -> new ConflictException("Organization with identifier '" + org.getIdentifier() + "' has not been registered yet!"))))
        .flatMap(this.organizationRepository::save);
  }

  @Override
  public Mono<Boolean> exists(Organization organization) {
    return Mono.just(organization)
        .map(Organization::getIdentifier)
        .flatMap(this::getOrganizationByIdentifier)
        .hasElement()
        .onErrorResume(NotFoundException.class, _ -> Mono.just(false));
  }

  @Override
  public Mono<Boolean> notExists(Organization organization) {
    return exists(organization).map(result -> !result);
  }

  @Override
  public Mono<Void> checkOrganizationDoesNotExists(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.makeSureAsync(
            this::notExists,
            ConflictException::new,
            org -> "Organization with identifier " + org.getIdentifier() + " already exists!"))
        .then();
  }

  @Override
  public Mono<Void> checkOrganizationExists(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.makeSureAsync(
            this::exists,
            ConflictException::new,
            org -> "Organization with identifier " + org.getIdentifier() + " has not been registered yet!"))
        .then();
  }

  @Override
  public Mono<Organization> getOrganizationByIdentifier(String identifier) {
    return MONO.combine(
        this.organizationRepository.findByIdentifier(identifier),
        this.trustedPartyRepository.findByOrganizationIdentifier(identifier),
        (organization, trustedParty) -> organization.withTrustedParty(trustedParty));
  }

  @Override
  public Mono<Organization> getOrganization(Organization organization) {
    return MONO.<Organization> makeSureNotNullWithMessage("Organization can not be null")
        .apply(organization)
        .map(Organization::getIdentifier)
        .flatMap(this::getOrganizationByIdentifier);
  }

  @Override
  public Mono<Void> storeDocument(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.liftOptionalToMono(
            Organization::getDocument,
            _ -> new InvalidValueException("Document has not been deserialized yet!")))
        .delayUntil(this.documentRepository::save)
        .delayUntil(this.organizationRepository.connectOwns(organization.getIdentifier()))
        .delayUntil(this.tokenRepository.connectWasIssuedBy(organization.getTrustedParty()))
        .then();

  }

}
