package org.commonprovenance.framework.store.service.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;

import reactor.core.publisher.Mono;

public interface TrustedPartyWebService {
  Mono<Void> registerOrganization(Organization organization);

  Mono<Void> updateOrganization(Organization organization);

  Mono<Boolean> organizationIsRegistered(Organization organization);

  Mono<Boolean> organizationIsNotRegistered(Organization organization);

  Function<Organization, Mono<Organization>> setTrustedPartyByBaseUrl(Optional<String> optTrustedPartyBaseUrl);

  Function<Organization, Mono<Void>> verifySignature(String signature);

  Function<Organization, Mono<Organization>> issueGraphToken(String signature);

  Mono<Organization> issueDomainSpecificGraphToken(Organization organization);

  Mono<Organization> issueBackboneGraphToken(Organization organization);
}
