package org.commonprovenance.framework.store.support;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.service.persistence.FinalizedProvComponentService;
import org.commonprovenance.framework.store.support.fixture.OrganizationFixture;
import org.commonprovenance.framework.store.support.fixture.ProvDocumentFixture;

public class IntegrationTestData {
  private final FinalizedProvComponentService finalizedProvComponentService;
  private final AppConfiguration configuration;

  public IntegrationTestData(
      FinalizedProvComponentService finalizedProvComponentService,
      AppConfiguration configuration) {
    this.finalizedProvComponentService = finalizedProvComponentService;
    this.configuration = configuration;
  }

  public Organization registerOrganization() {
    return finalizedProvComponentService.getDefaultTrustedParty()
        .map(OrganizationFixture::random)
        .delayUntil(finalizedProvComponentService::storeOrganization)
        .block();
  }

  public ProvDocumentFixture provDocument(String templatePath, Organization organization) {
    return ProvDocumentFixture.of(templatePath, configuration.getFqdn(), organization.getIdentifier());
  }
}
