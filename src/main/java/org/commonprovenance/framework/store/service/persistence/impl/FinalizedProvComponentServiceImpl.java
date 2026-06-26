package org.commonprovenance.framework.store.service.persistence.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.utils.TrustedPartyUtils;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.OrganizationRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TrustedPartyRepository;
import org.commonprovenance.framework.store.service.persistence.FinalizedProvComponentService;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
import reactor.core.publisher.Mono;

@Service
public class FinalizedProvComponentServiceImpl implements FinalizedProvComponentService {
  private final OrganizationRepository organizationRepository;
  private final TrustedPartyRepository trustedPartyRepository;
  private final DocumentRepository documentRepository;
  private final TokenRepository tokenRepository;

  public FinalizedProvComponentServiceImpl(
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
        .delayUntil(MONO.makeSureNotAsync(
            org -> Mono.just(org)
                .map(Organization::getIdentifier)
                .flatMap(this.organizationRepository::existsByIdentifier),
            org -> new ConflictException("Organization with identifier '" + org.getIdentifier() + "' already registered in CPF-Store!")))
        .delayUntil(this.organizationRepository::save)
        .delayUntil(this.organizationRepository::connectTrusts)
        .then();
  }

  @Override
  public Mono<Void> storeTrustedParty(TrustedParty trustedParty) {
    return MONO.<TrustedParty> makeSureNotNullWithMessage("TrustedParty can not be null").apply(trustedParty)
        .flatMap(this.trustedPartyRepository::create);
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

  @Override
  public Mono<Void> updateOrganization(Organization organization) {
    return Mono.just(organization)
        .flatMap((MONO.makeSureAsync(
            org -> Mono.just(org)
                .map(Organization::getIdentifier)
                .flatMap(this.organizationRepository::existsByIdentifier),
            org -> new ConflictException("Organization with identifier '" + org.getIdentifier() + "' has not been registered yet!"))))
        .delayUntil(this.organizationRepository::save)
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
  public Mono<String> getOrganizationIdentifierByDocumentIdentifier(String identifier) {
    return this.documentRepository.getOrganizationIdentifierByIdentifier(identifier);
  }

  @Override
  public Mono<Document> getDocumentByIdentifier(String identifier) {
    return this.documentRepository.findByIdentifier(identifier);
  }

  @Override
  public Mono<Void> checkDocumentDoesNotExists(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.liftOptionalToMono(
            Organization::getDocument,
            _ -> new InvalidValueException("Document has not been deserialized yet!")))
        .flatMap(MONO.liftEffectToMono(Document::getIdentifier))
        .flatMap(MONO.makeSureNotAsync(
            documentRepository::existsByIdentifier,
            ConflictException::new,
            identifier -> "Document with identifier '" + identifier + "' exists!!"))
        .then();
  }

  @Override
  public Mono<TrustedParty> getDefaultTrustedParty() {
    return this.trustedPartyRepository.findDefault();
  }

  @Override
  public Mono<Boolean> isTrustedPartyValid(Organization organization) {
    return Mono.just(organization)
        .flatMap(MONO.liftOptionalToMono(Organization::getTrustedParty))
        .map(TrustedParty::getName)
        .flatMap(this.trustedPartyRepository::findByName)
        .delayUntil(MONO.liftEffectToMono(TrustedPartyUtils::validate))
        .hasElement()
        .onErrorReturn(NotFoundException.class, false);
  }

}
