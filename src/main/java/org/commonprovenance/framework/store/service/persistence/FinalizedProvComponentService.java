package org.commonprovenance.framework.store.service.persistence;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface FinalizedProvComponentService {

  Mono<Void> storeOrganization(Organization organization);

  Mono<Void> storeTrustedParty(TrustedParty trustedParty);

  Mono<Void> storeDocument(Organization organization);

  Mono<Void> updateOrganization(Organization organization);

  Mono<Organization> getOrganizationByIdentifier(String identifier);

  Mono<String> getOrganizationIdentifierByDocumentIdentifier(String identifier);

  Mono<Document> getDocumentByIdentifier(String identifier);

  Mono<Void> checkDocumentDoesNotExists(Organization organization);

  Mono<TrustedParty> getDefaultTrustedParty();

  Mono<Boolean> isTrustedPartyValid(Organization organization);
}
