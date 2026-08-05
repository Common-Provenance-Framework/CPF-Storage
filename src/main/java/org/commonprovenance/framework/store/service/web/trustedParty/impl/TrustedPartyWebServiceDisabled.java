package org.commonprovenance.framework.store.service.web.trustedParty.impl;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.service.web.trustedParty.TrustedPartyWebService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
@ConditionalOnProperty(prefix = "trusted-party", name = "enabled", havingValue = "false")
public class TrustedPartyWebServiceDisabled implements TrustedPartyWebService {

  @Override
  public Mono<Void> registerOrganization(Organization organization) {
    return Mono.empty();
  }

  @Override
  public Mono<Void> updateOrganization(Organization organization) {
    return Mono.empty();
  }

  @Override
  public Mono<Boolean> organizationIsRegistered(Organization organization) {
    return Mono.just(false);
  }

  @Override
  public Mono<Boolean> organizationIsNotRegistered(Organization organization) {
    return Mono.just(true);
  }

  @Override
  public Function<Organization, Mono<Organization>> setTrustedPartyByBaseUrl(Optional<String> optTrustedPartyBaseUrl) {
    return Mono::just;
  }

  @Override
  public Function<Organization, Mono<Void>> verifySignature(String signature) {
    return organization -> Mono.empty();
  }

  @Override
  public Function<Organization, Mono<Organization>> issueGraphToken(String signature) {
    return Mono::just;
  }

  @Override
  public Mono<Organization> issueDomainSpecificGraphToken(Organization organization) {
    return Mono.just(organization);
  }

  @Override
  public Mono<Organization> issueBackboneGraphToken(Organization organization) {
    return Mono.just(organization);
  }
}
