package org.commonprovenance.framework.store.service.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

public interface TrustedPartyWebService {
  Function<Organization, Mono<Organization>> createOrganization(Optional<String> trustedPartyUri);

  Mono<Organization> updateOrganization(Organization organization);

  Mono<Boolean> exists(Organization organization);

  Mono<Boolean> notExists(Organization organization);

  Mono<TrustedParty> getTrustedPartyByUrl(Optional<String> trustedPartyUrl);

  Function<Organization, Mono<Void>> verifySignature(Document document);

  Function<Document, Mono<Token>> issueGraphToken(Optional<String> trustedPartyUrl);

  Mono<Token> issueGraphToken(Document document);

  Function<Document, Mono<Token>> issueDomainSpecificGraphToken(Optional<String> trustedPartyUrl);

  Function<Document, Mono<Token>> issueBackboneGraphToken(Optional<String> trustedPartyUrl);
}
