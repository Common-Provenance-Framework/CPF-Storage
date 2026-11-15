package org.commonprovenance.framework.store.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.commonprovenance.framework.store.support.fixture.MetaDocuments.metaDocumentWithVersions;
import static org.commonprovenance.framework.store.support.fixture.TrustedPartyFixture.issuedTokenFor;
import static org.commonprovenance.framework.store.support.store.StoreExpectations.issuedToken;
import static org.commonprovenance.framework.store.support.store.StoreExpectations.metaDocument;

import org.commonprovenance.framework.store.support.IntegrationTest;
import org.commonprovenance.framework.store.support.IntegrationTestData;
import org.commonprovenance.framework.store.support.fixture.OrganizationData;
import org.commonprovenance.framework.store.support.fixture.ProvDocumentFixture;
import org.commonprovenance.framework.store.support.fixture.StoredDocument;
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
@DisplayName("Integration - Meta documents")
class MetaDocumentIT {

  @Autowired
  private StoreClient store;

  @Autowired
  private TrustedPartyStub trustedParty;

  @Autowired
  private IntegrationTestData testData;

  @Test
  @DisplayName("should create the meta document when the first version of a document is uploaded")
  void exists_shouldConfirmMetaDocumentCreatedByUpload() {
    OrganizationData organization = testData.registeredOrganization();
    ProvDocumentFixture document = testData.newDocumentOf(organization);
    String token = issuedTokenFor(document);

    trustedParty.issuesGraphTokenFor(document, token);

    var uploaded = store.uploadDocument(document);
    var checked = store.checkMetaDocumentExists(document);

    assertThat(uploaded)
        .hasStatus(HttpStatus.CREATED)
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(TokenView.class).isEqualTo(issuedToken(token));
    assertThat(checked)
        .hasStatusOk()
        .body().isEmpty();
  }

  @Test
  @DisplayName("should return the meta document with a token issued for it")
  void getMetaDocument_shouldReturnMetaDocumentWithIssuedToken() {
    StoredDocument stored = testData.storedDocument();
    String metaToken = issuedTokenFor(stored.document());

    trustedParty.issuesMetaTokenFor(stored.document(), metaToken);

    var meta = store.getMetaDocument(stored.document());

    assertThat(meta)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(DocumentView.class).isEqualTo(metaDocument(metaDocumentWithVersions(stored), metaToken));
  }

  @Test
  @DisplayName("should add the uploaded next version of a document into its meta document")
  void createProvDocument_shouldAddNextVersionIntoMetaDocument() {
    StoredDocument firstVersion = testData.storedDocument();
    ProvDocumentFixture nextVersion = firstVersion.document().nextVersion();
    StoredDocument secondVersion = new StoredDocument(nextVersion, issuedTokenFor(nextVersion));
    String metaToken = issuedTokenFor(nextVersion);

    trustedParty.issuesGraphTokenFor(secondVersion.document(), secondVersion.token());
    trustedParty.issuesMetaTokenFor(secondVersion.document(), metaToken);

    var uploaded = store.uploadDocument(secondVersion.document());
    var meta = store.getMetaDocument(secondVersion.document());

    assertThat(uploaded)
        .hasStatus(HttpStatus.CREATED)
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(TokenView.class).isEqualTo(issuedToken(secondVersion.token()));
    assertThat(meta)
        .hasStatusOk()
        .hasContentType(MediaType.APPLICATION_JSON)
        .bodyJson().convertTo(DocumentView.class).isEqualTo(metaDocument(metaDocumentWithVersions(firstVersion, secondVersion), metaToken));
  }
}
