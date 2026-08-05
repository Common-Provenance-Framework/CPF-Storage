package org.commonprovenance.framework.store.service.web.trustedParty.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Organization;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

@DisplayName("No-op TrustedParty service")
class TrustedPartyWebServiceDisabledTest {

  private final TrustedPartyWebServiceDisabled service = new TrustedPartyWebServiceDisabled();

  @Test
  @DisplayName("should no-op registration and update calls")
  void shouldNoOpRegistrationAndUpdateCalls() {
    Organization organization = new Organization();

    StepVerifier.create(service.registerOrganization(organization)).verifyComplete();
    StepVerifier.create(service.updateOrganization(organization)).verifyComplete();
  }

  @Test
  @DisplayName("should report organization as not registered and preserve organization")
  void shouldReportOrganizationAsNotRegisteredAndPreserveOrganization() {
    Organization organization = new Organization();

    StepVerifier.create(service.organizationIsRegistered(organization))
        .expectNext(false)
        .verifyComplete();

    StepVerifier.create(service.organizationIsNotRegistered(organization))
        .expectNext(true)
        .verifyComplete();

    StepVerifier.create(service.setTrustedPartyByBaseUrl(Optional.empty()).apply(organization))
        .assertNext(result -> assertEquals(organization, result))
        .verifyComplete();
  }

  @Test
  @DisplayName("should preserve organization for token issuance operations")
  void shouldPreserveOrganizationForTokenIssuanceOperations() {
    Organization organization = new Organization();

    StepVerifier.create(service.verifySignature("sig").apply(organization))
        .verifyComplete();

    StepVerifier.create(service.issueGraphToken("sig").apply(organization))
        .assertNext(result -> assertEquals(organization, result))
        .verifyComplete();

    StepVerifier.create(service.issueDomainSpecificGraphToken(organization))
        .assertNext(result -> assertEquals(organization, result))
        .verifyComplete();

    StepVerifier.create(service.issueBackboneGraphToken(organization))
        .assertNext(result -> assertEquals(organization, result))
        .verifyComplete();
  }
}
