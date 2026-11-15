package org.commonprovenance.framework.store.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.commonprovenance.framework.store.support.fixture.OrganizationData.newOrganization;
import static org.commonprovenance.framework.store.support.store.StoreExpectations.registeredOrganization;

import org.commonprovenance.framework.store.support.IntegrationTest;
import org.commonprovenance.framework.store.support.IntegrationTestData;
import org.commonprovenance.framework.store.support.fixture.OrganizationData;
import org.commonprovenance.framework.store.support.store.OrganizationView;
import org.commonprovenance.framework.store.support.store.StoreClient;
import org.commonprovenance.framework.store.support.trustedParty.TrustedPartyStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@IntegrationTest
@DisplayName("Integration - Organizations")
class OrganizationIT {

  @Autowired
  private StoreClient store;

  @Autowired
  private TrustedPartyStub trustedParty;

  @Autowired
  private IntegrationTestData testData;

  @Test
  @DisplayName("should register the organization in the store and in TrustedParty")
  void createOrganization_shouldRegisterOrganizationInStoreAndTrustedParty() {
    OrganizationData organization = newOrganization();

    trustedParty.providesItsInfo();
    trustedParty.doesNotKnow(organization);
    trustedParty.acceptsRegistrationOf(organization);

    var registered = store.registerOrganization(organization);
    var fetched = store.getOrganization(organization);

    assertThat(registered)
        .hasStatus(HttpStatus.CREATED)
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(OrganizationView.class).isEqualTo(registeredOrganization(organization));
    assertThat(fetched)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(OrganizationView.class).isEqualTo(registeredOrganization(organization));
  }

  @Test
  @DisplayName("should return the registered organization")
  void getOrganizationByIdentifier_shouldReturnRegisteredOrganization() {
    OrganizationData organization = testData.registeredOrganization();

    var fetched = store.getOrganization(organization);

    assertThat(fetched)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(OrganizationView.class).isEqualTo(registeredOrganization(organization));
  }

  @Test
  @DisplayName("should update the organization certificates in the store and in TrustedParty")
  void updateOrganization_shouldUpdateCertificatesInStoreAndTrustedParty() {
    OrganizationData organization = testData.registeredOrganization();
    OrganizationData renewed = organization.withRenewedCertificates();

    trustedParty.knows(organization);
    trustedParty.acceptsCertificateUpdateOf(renewed);

    var updated = store.updateCertificates(renewed);
    var fetched = store.getOrganization(renewed);

    assertThat(updated)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(OrganizationView.class).isEqualTo(registeredOrganization(renewed));
    assertThat(fetched)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(OrganizationView.class).isEqualTo(registeredOrganization(renewed));
  }
}
