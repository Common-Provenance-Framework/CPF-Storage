package org.commonprovenance.framework.store.support.store;

import org.commonprovenance.framework.store.support.ProvGraph;
import org.commonprovenance.framework.store.support.fixture.OrganizationData;
import org.commonprovenance.framework.store.support.fixture.ProvDocumentFixture;

public final class StoreExpectations {

  private StoreExpectations() {
  }

  public static OrganizationView registeredOrganization(OrganizationData organization) {
    return new OrganizationView(organization.id(), organization.clientCertificate(), organization.intermediateCertificates());
  }

  public static TokenView issuedToken(String token) {
    return new TokenView(token);
  }

  public static DocumentView storedDocument(ProvDocumentFixture document, String token) {
    return new DocumentView(ProvGraph.of(document.json()), token);
  }

  public static DocumentView domainSpecificViewOf(ProvDocumentFixture document, String token) {
    return new DocumentView(ProvGraph.of(document.domainSpecificView()), token);
  }

  public static DocumentView backboneViewOf(ProvDocumentFixture document, String token) {
    return new DocumentView(ProvGraph.of(document.backboneView()), token);
  }

  public static DocumentView metaDocument(ProvGraph metaDocument, String token) {
    return new DocumentView(metaDocument, token);
  }
}
