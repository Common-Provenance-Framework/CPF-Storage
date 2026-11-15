package org.commonprovenance.framework.store.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.support.fixture.OrganizationData;
import org.commonprovenance.framework.store.support.fixture.ProvDocumentFixture;
import org.commonprovenance.framework.store.support.fixture.StoredDocument;
import org.commonprovenance.framework.store.support.fixture.TrustedPartyFixture;
import org.commonprovenance.framework.store.support.store.StoreClient;
import org.commonprovenance.framework.store.support.trustedParty.TrustedPartyStub;
import org.springframework.http.HttpStatus;

public class IntegrationTestData {
  private final StoreClient store;
  private final TrustedPartyStub trustedParty;
  private final AppConfiguration configuration;

  public IntegrationTestData(StoreClient store, TrustedPartyStub trustedParty, AppConfiguration configuration) {
    this.store = store;
    this.trustedParty = trustedParty;
    this.configuration = configuration;
  }

  public OrganizationData registeredOrganization() {
    OrganizationData organization = OrganizationData.newOrganization();
    trustedParty.providesItsInfo();
    trustedParty.doesNotKnow(organization);
    trustedParty.acceptsRegistrationOf(organization);
    assertThat(store.registerOrganization(organization)).hasStatus(HttpStatus.CREATED);
    return organization;
  }

  public ProvDocumentFixture newDocumentOf(OrganizationData organization) {
    return ProvDocumentFixture.of(ProvDocumentFixture.MAIN_ACTIVITY_ONLY, configuration.getFqdn(), organization.id());
  }

  public ProvDocumentFixture newDocumentConnectedTo(OrganizationData organization, ProvDocumentFixture referenced) {
    return newDocumentOf(organization).connectedTo(referenced);
  }

  public StoredDocument storedDocument() {
    return uploaded(newDocumentOf(registeredOrganization()));
  }

  public StoredDocument storedDocumentWithDomainSpecificPart() {
    return uploaded(ProvDocumentFixture.of(ProvDocumentFixture.WITH_DOMAIN_SPECIFIC_PART, configuration.getFqdn(), registeredOrganization().id()));
  }

  private StoredDocument uploaded(ProvDocumentFixture document) {
    String token = TrustedPartyFixture.issuedTokenFor(document);
    trustedParty.issuesGraphTokenFor(document, token);
    assertThat(store.uploadDocument(document)).hasStatus(HttpStatus.CREATED);
    return new StoredDocument(document, token);
  }
}
