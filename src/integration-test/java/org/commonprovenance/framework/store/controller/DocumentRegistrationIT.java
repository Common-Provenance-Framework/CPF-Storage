package org.commonprovenance.framework.store.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.support.IntegrationTest;
import org.commonprovenance.framework.store.support.IntegrationTestData;
import org.commonprovenance.framework.store.support.StoreApi;
import org.commonprovenance.framework.store.support.fixture.ProvDocumentFixture;
import org.commonprovenance.framework.store.support.fixture.TrustedPartyFixture;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyWeb;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@IntegrationTest
@AutoConfigureWebTestClient
@Import(IntegrationTestData.class)
@DisplayName("Integration - Document registration")
class DocumentRegistrationIT {

  private static final String SIGNATURE = "document-signature";

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private IntegrationTestData testData;

  @MockitoSpyBean
  private TrustedPartyWeb trustedPartyWeb;

  @Test
  @DisplayName("should store a valid document and return the token issued by TrustedParty")
  void createProvDocument_shouldStoreDocumentAndReturnIssuedToken() {
    Organization organization = testData.registerOrganization();
    ProvDocumentFixture document = testData.provDocument("fixtures/documents/main-activity-only.json", organization);
    Token token = TrustedPartyFixture.issueToken(document);
    when(trustedPartyWeb.issueGraphToken(SIGNATURE)).thenReturn(_ -> Mono.just(token));

    webTestClient.post()
        .uri(StoreApi.DOCUMENTS, organization.getIdentifier())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(document.toForm(SIGNATURE))
        .exchange()
        .expectStatus().isCreated()
        .expectBody(TokenResponseDTO.class)
        .isEqualTo(new TokenResponseDTO(token.getJwt()));

    verify(trustedPartyWeb).verifySignature(SIGNATURE);

    // Refetch to verify
    webTestClient.get()
        .uri(StoreApi.DOCUMENT_TOKEN, organization.getIdentifier(), document.bundleId())
        .exchange()
        .expectStatus().isOk()
        .expectBody(TokenResponseDTO.class)
        .isEqualTo(new TokenResponseDTO(token.getJwt()));
  }
}
