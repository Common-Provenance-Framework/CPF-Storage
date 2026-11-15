package org.commonprovenance.framework.store.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.commonprovenance.framework.store.support.fixture.TrustedPartyFixture.issuedTokenFor;
import static org.commonprovenance.framework.store.support.store.StoreExpectations.backboneViewOf;
import static org.commonprovenance.framework.store.support.store.StoreExpectations.domainSpecificViewOf;
import static org.commonprovenance.framework.store.support.store.StoreExpectations.issuedToken;
import static org.commonprovenance.framework.store.support.store.StoreExpectations.storedDocument;

import org.commonprovenance.framework.store.support.IntegrationTest;
import org.commonprovenance.framework.store.support.IntegrationTestData;
import org.commonprovenance.framework.store.support.fixture.OrganizationData;
import org.commonprovenance.framework.store.support.fixture.ProvDocumentFixture;
import org.commonprovenance.framework.store.support.fixture.StoredDocument;
import org.commonprovenance.framework.store.support.otherStore.OtherStoreStub;
import org.commonprovenance.framework.store.support.store.DocumentView;
import org.commonprovenance.framework.store.support.store.StoreClient;
import org.commonprovenance.framework.store.support.store.TokenView;
import org.commonprovenance.framework.store.support.trustedParty.TrustedPartyStub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@IntegrationTest
@DisplayName("Integration - Documents")
class DocumentIT {

  @Autowired
  private StoreClient store;

  @Autowired
  private TrustedPartyStub trustedParty;

  @Autowired
  private OtherStoreStub otherStore;

  @Autowired
  private IntegrationTestData testData;

  @Test
  @DisplayName("should store the document and return the token issued by TrustedParty")
  void createProvDocument_shouldStoreDocumentAndReturnIssuedToken() {
    OrganizationData organization = testData.registeredOrganization();
    ProvDocumentFixture document = testData.newDocumentOf(organization);
    String token = issuedTokenFor(document);

    trustedParty.issuesGraphTokenFor(document, token);

    var uploaded = store.uploadDocument(document);
    var fetched = store.getDocument(document);

    assertThat(uploaded)
        .hasStatus(HttpStatus.CREATED)
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(TokenView.class).isEqualTo(issuedToken(token));
    assertThat(fetched)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(DocumentView.class).isEqualTo(storedDocument(document, token));
  }

  @Test
  @DisplayName("should store a document connected to a bundle in another store after checking the connector")
  void createProvDocument_shouldStoreDocumentConnectedToBundleInAnotherStore() {
    OrganizationData organization = testData.registeredOrganization();
    ProvDocumentFixture referenced = otherStore.newDocument();
    ProvDocumentFixture document = testData.newDocumentConnectedTo(organization, referenced);
    String referencedToken = issuedTokenFor(referenced);
    String token = issuedTokenFor(document);

    otherStore.holds(referenced, referencedToken);
    trustedParty.issuesGraphTokenFor(document, token);

    var uploaded = store.uploadDocument(document);
    var fetched = store.getDocument(document);

    assertThat(uploaded)
        .hasStatus(HttpStatus.CREATED)
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(TokenView.class).isEqualTo(issuedToken(token));
    assertThat(fetched)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(DocumentView.class).isEqualTo(storedDocument(document, token));
  }

  @Test
  @DisplayName("should return the token of the stored document")
  void getToken_shouldReturnTokenOfStoredDocument() {
    StoredDocument stored = testData.storedDocument();

    var fetched = store.getDocumentToken(stored.document());

    assertThat(fetched)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(TokenView.class).isEqualTo(issuedToken(stored.token()));
  }

  @Test
  @DisplayName("should return the stored document")
  void getFinalizedProvDocumentByIdentifier_shouldReturnStoredDocument() {
    StoredDocument stored = testData.storedDocument();

    var fetched = store.getDocument(stored.document());

    assertThat(fetched)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(DocumentView.class).isEqualTo(storedDocument(stored.document(), stored.token()));
  }

  @Test
  @DisplayName("should confirm that the stored document exists")
  void exists_shouldConfirmStoredDocumentExists() {
    StoredDocument stored = testData.storedDocument();

    var checked = store.checkDocumentExists(stored.document());

    assertThat(checked)
        .hasStatusOk()
        .body().isEmpty();
  }

  @Test
  @DisplayName("should return the domain-specific part of the stored document with a token issued for it")
  void getDomainProvDocumentByIdentifier_shouldReturnDomainSpecificPartWithIssuedToken() {
    StoredDocument stored = testData.storedDocumentWithDomainSpecificPart();
    String token = issuedTokenFor(stored.document());

    trustedParty.issuesDomainSpecificTokenFor(stored.document(), token);

    var view = store.getDomainSpecificView(stored.document());

    assertThat(view)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(DocumentView.class).isEqualTo(domainSpecificViewOf(stored.document(), token));
  }

  @Test
  @DisplayName("should return the backbone of the stored document with a token issued for it")
  void getBackboneProvDocumentByIdentifier_shouldReturnBackboneWithIssuedToken() {
    StoredDocument stored = testData.storedDocumentWithDomainSpecificPart();
    String token = issuedTokenFor(stored.document());

    trustedParty.issuesBackboneTokenFor(stored.document(), token);

    var view = store.getBackboneView(stored.document());

    assertThat(view)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(DocumentView.class).isEqualTo(backboneViewOf(stored.document(), token));
  }
}
