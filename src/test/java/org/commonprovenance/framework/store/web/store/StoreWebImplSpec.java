package org.commonprovenance.framework.store.web.store;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.store.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.web.store.impl.StoreWebImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.vavr.control.Either;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreWeb Spec")
class StoreWebImplSpec {

  private static final String STORE_URL = "http://localhost:8080/api/v1/";
  private static final String STORE_URL_EXT = "http://localhost:80/api/v1/";
  private static final String RESOURCE_PATH = "organizations/079ad9d01e7c/documents/8c46667151db";

  @Mock
  private Client client;

  @Mock
  private WebClient storeWebClient;

  @Mock
  private WebClient customWebClient;

  @Mock
  private TokenResponseDTO tokenDTO;

  @Mock
  private Token token;

  private StoreWeb storeWeb;
  private ListAppender<ILoggingEvent> logAppender;
  private Logger storeWebLogger;

  @BeforeEach
  void setUp() {
    storeWeb = new StoreWebImpl(client);
    when(client.getStoreUrl()).thenReturn(STORE_URL);

  }

  @BeforeEach
  void setUpLogCapture() {
    storeWebLogger = (Logger) LoggerFactory.getLogger(StoreWebImpl.class);
    storeWebLogger.setLevel(Level.TRACE); // success messages are logged at TRACE
    logAppender = new ListAppender<>();
    logAppender.start();
    storeWebLogger.addAppender(logAppender);
  }

  @AfterEach
  void tearDownLogCapture() {
    storeWebLogger.detachAppender(logAppender);
  }

  @Nested
  @DisplayName("pingByResourcePath")
  class PingByResourcePath {
    @Test
    @DisplayName("Ping current store")
    void pingByResourcePath_current() {
      when(client.store()).thenReturn(storeWebClient);
      when(client.sendHeadRequest(any(WebClient.class), anyString(), anyMap())).thenReturn(Mono.empty());

      storeWeb.pingByResourcePath(STORE_URL + RESOURCE_PATH)
          .block();

      verify(
          client,
          description("Current WebClient has been loaded"))
          .store();

      verify(client, never()).custom(anyString());

      verify(
          client,
          description("HEAD request should be called with the store client and resource path with empty query params"))
          .sendHeadRequest(storeWebClient, RESOURCE_PATH, Map.of());
    }

    @Test
    @DisplayName("Ping external store")
    void pingByResourcePath_external() {
      when(client.custom(anyString())).thenReturn(customWebClient);
      when(client.sendHeadRequest(any(WebClient.class), anyString(), anyMap())).thenReturn(Mono.empty());

      storeWeb.pingByResourcePath(STORE_URL_EXT + RESOURCE_PATH)
          .block();

      verify(
          client,
          description("External WebClient has been loaded"))
          .custom(STORE_URL_EXT + RESOURCE_PATH);
      verify(client, never()).store();

      verify(
          client,
          description("HEAD request should be called with the external store client with empty resource path and query params"))
          .sendHeadRequest(customWebClient, "", Map.of());
    }
  }

  @Nested
  @DisplayName("getTokenByResourcePath")
  class GetTokenByResourcePath {
    @Test
    @DisplayName("Fetch token from current store")
    void getTokenByResourcePath_current() {
      try (MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {
        tokenFactory.when(() -> TokenFactory.build(any(TokenResponseDTO.class))).thenReturn(Either.right(token));

        when(client.store()).thenReturn(storeWebClient);
        when(client.sendGetOneRequest(any(WebClient.class), anyString(), any(), anyMap()))
            .thenReturn(Mono.just(tokenDTO));

        storeWeb.getTokenByResourcePath(STORE_URL + RESOURCE_PATH)
            .block();

        verify(
            client,
            description("Current WebClient has been loaded"))
            .store();
        verify(client, never()).custom(anyString());

        verify(
            client,
            description("GET request should be called with the store client and resource path in uri"))
            .sendGetOneRequest(storeWebClient, RESOURCE_PATH + "/token", TokenResponseDTO.class, Map.of());

        tokenFactory.verify(
            () -> TokenFactory.build(tokenDTO),
            description("Token factory has been called with exact response"));

      }
    }

    @Test
    @DisplayName("Fetch token from external store")
    void getTokenByResourcePath_externala() {
      try (MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {
        tokenFactory.when(() -> TokenFactory.build(any(TokenResponseDTO.class))).thenReturn(Either.right(token));

        when(client.custom(anyString())).thenReturn(customWebClient);
        when(client.sendGetOneRequest(any(WebClient.class), anyString(), any(), anyMap()))
            .thenReturn(Mono.just(tokenDTO));

        storeWeb.getTokenByResourcePath(STORE_URL_EXT + RESOURCE_PATH)
            .block();

        verify(
            client,
            description("External WebClient has been loaded"))
            .custom(STORE_URL_EXT + RESOURCE_PATH);
        verify(client, never()).store();

        verify(
            client,
            description("GET request should be called with the custom client and /token in uri"))
            .sendGetOneRequest(customWebClient, "/token", TokenResponseDTO.class, Map.of());

        tokenFactory.verify(
            () -> TokenFactory.build(tokenDTO),
            description("Token factory has been called with exact response"));

      }
    }

  }
}
