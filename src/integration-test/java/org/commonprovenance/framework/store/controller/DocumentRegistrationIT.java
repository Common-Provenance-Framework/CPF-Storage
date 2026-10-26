package org.commonprovenance.framework.store.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.model.GraphFormat;
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
  // Pushes the request body well past Spring's 256KB default codec limit
  private static final int PADDING_BYTES = 512 * 1024;

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

  @Test
  @DisplayName("should store a document larger than Spring's default 256KB codec limit")
  void createProvDocument_shouldStoreDocumentLargerThanDefaultCodecLimit() {
    Organization organization = testData.registerOrganization();
    ProvDocumentFixture document = testData.provDocument("fixtures/documents/main-activity-only.json", organization);
    Token token = TrustedPartyFixture.issueToken(document);
    when(trustedPartyWeb.issueGraphToken(SIGNATURE)).thenReturn(_ -> Mono.just(token));

    // Trailing whitespace keeps the JSON valid while making the request body large.
    String paddedJson = document.json() + " ".repeat(PADDING_BYTES);
    String encodedGraph = Base64.getEncoder().encodeToString(paddedJson.getBytes(StandardCharsets.UTF_8));

    webTestClient.post()
        .uri(StoreApi.DOCUMENTS, organization.getIdentifier())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new DocumentFormDTO(encodedGraph, GraphFormat.JSON, SIGNATURE))
        .exchange()
        .expectStatus().isCreated()
        .expectBody(TokenResponseDTO.class)
        .isEqualTo(new TokenResponseDTO(token.getJwt()));
  }
}
