package org.commonprovenance.framework.store.web.trustedParty;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.impl.TokenWebImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openprovenance.prov.model.QualifiedName;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.vavr.control.Either;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenWeb Spec")
class TokenWebSpec {
  private static final String TP_URL = "http://localhost:80/api/v1";
  private static final String ORGANIZATION_ID = "2642ab32-978c-4b24-817d-fed1222664e8";
  private static final String DOCUMENT_ID = "b00abc48-ddd7-47ad-bb19-9f5240533be0";
  private static final String TOKENS_URI = "/organizations/" + ORGANIZATION_ID + "/tokens";

  @Mock
  private Client client;

  @Mock
  private WebClient defaultTpWebClient;

  @Mock
  private WebClient customTpWebClient;

  @Mock
  private TokenResponseDTO tokenResponseDTO;

  @Mock
  private Token token;

  @Mock
  private QualifiedName bundleIdentifier;

  private TokenWeb tokenWeb;
  private ListAppender<ILoggingEvent> logAppender;
  private Logger tokenWebLogger;

  @BeforeEach
  void setUp() {
    tokenWeb = new TokenWebImpl(client);
  }

  @BeforeEach
  void setUpLogCapture() {
    tokenWebLogger = (Logger) LoggerFactory.getLogger(TokenWebImpl.class);
    tokenWebLogger.setLevel(Level.TRACE);
    logAppender = new ListAppender<>();
    logAppender.start();
    tokenWebLogger.addAppender(logAppender);
  }

  @AfterEach
  void tearDownLogCapture() {
    tokenWebLogger.detachAppender(logAppender);
  }

  @Nested
  @DisplayName("getAllByOrganization")
  class GetAllByOrganization {
    @Test
    @DisplayName("Fetch all tokens using the default trusted party")
    void getAllByOrganization_defaultTrustedParty() {
      assertGetAllByOrganization(true);
    }

    @Test
    @DisplayName("Fetch all tokens using a custom trusted party")
    void getAllByOrganization_customTrustedParty() {
      assertGetAllByOrganization(false);
    }

    private void assertGetAllByOrganization(Boolean isDefault) {
      WebClient webClient = isDefault ? defaultTpWebClient : customTpWebClient;
      Optional<String> maybeUrl = isDefault ? Optional.empty() : Optional.of(TP_URL);

      try (MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {
        tokenFactory.when(() -> TokenFactory.build(tokenResponseDTO)).thenReturn(Either.right(token));
        when(client.trustedParty(maybeUrl)).thenReturn(webClient);
        when(client.sendGetManyRequest(
            webClient, TOKENS_URI, TokenResponseDTO.class, Map.of()))
            .thenReturn(Flux.just(tokenResponseDTO, tokenResponseDTO));

        tokenWeb.getAllByOrganization(maybeUrl)
            .apply(ORGANIZATION_ID)
            .collectList()
            .block();

        verify(client).trustedParty(maybeUrl);
        verify(client).sendGetManyRequest(webClient, TOKENS_URI, TokenResponseDTO.class, Map.of());
        tokenFactory.verify(() -> TokenFactory.build(tokenResponseDTO), times(2));
      }
    }
  }

  @Nested
  @DisplayName("getByDocumentId")
  class GetByDocumentId {
    @Test
    @DisplayName("Fetch token using the default trusted party")
    void getByDocumentId_default() {
      assertGetByDocumentId(true);
    }

    @Test
    @DisplayName("Fetch token using the custom trusted party")
    void getByDocumentId_custom() {
      assertGetByDocumentId(true);
    }

    private void assertGetByDocumentId(Boolean isDefault) {
      String tokenUri = TOKENS_URI + "/" + DOCUMENT_ID + "/" + GraphFormat.JSON;
      WebClient webClient = isDefault ? defaultTpWebClient : customTpWebClient;
      Optional<String> maybeUrl = isDefault ? Optional.empty() : Optional.of(TP_URL);

      try (MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {
        tokenFactory.when(() -> TokenFactory.build(tokenResponseDTO)).thenReturn(Either.right(token));
        when(bundleIdentifier.getLocalPart()).thenReturn(DOCUMENT_ID);

        when(client.trustedParty(maybeUrl)).thenReturn(webClient);
        when(client.sendGetManyRequest(webClient, tokenUri, TokenResponseDTO.class, Map.of()))
            .thenReturn(Flux.just(tokenResponseDTO));

        tokenWeb.getByDocumentId(ORGANIZATION_ID, bundleIdentifier, GraphFormat.JSON, maybeUrl)
            .collectList()
            .block();

        verify(client).trustedParty(maybeUrl);
        verify(client).sendGetManyRequest(webClient, tokenUri, TokenResponseDTO.class, Map.of());
        tokenFactory.verify(() -> TokenFactory.build(tokenResponseDTO), times(1));
      }
    }

  }
}
