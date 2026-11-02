package org.commonprovenance.framework.store.web.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.commonprovenance.framework.store.controller.dto.error.BadRequestDTO;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.vavr.control.Either;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreWeb Spec")
class StoreWebSpec {
  private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Ping store resource using store WebClient and its relative URI")
      void pingByResourcePath_usesStoreClientForStoreResource() {
        when(client.store()).thenReturn(storeWebClient);
        when(client.sendHeadRequest(storeWebClient, RESOURCE_PATH, Map.of())).thenReturn(Mono.empty());

        storeWeb.pingByResourcePath(STORE_URL + RESOURCE_PATH)
            .block();

        verify(
            client,
            description("HEAD request should be called with the store client and resource path"))
            .sendHeadRequest(storeWebClient, RESOURCE_PATH, Map.of());
      }

      @Test
      @DisplayName("Ping custom resource using its base URL with resource path")
      void pingByResourcePath_usesCustomClientForExternalResource() {
        when(client.custom(anyString())).thenReturn(customWebClient);
        when(client.sendHeadRequest(customWebClient, "", Map.of())).thenReturn(Mono.empty());

        storeWeb.pingByResourcePath(STORE_URL_EXT + RESOURCE_PATH)
            .block();

        verify(
            client,
            description("Custom client has to be set with exact base url"))
            .custom(STORE_URL_EXT + RESOURCE_PATH);

        verify(
            client,
            description("HEAD request should be called with the exact custom client and empty resource path"))
            .sendHeadRequest(customWebClient, "", Map.of());
      }

      @Test
      @DisplayName("Log exact message if ping passed")
      void pingByResourcePath_logMessage() {
        when(client.store()).thenReturn(storeWebClient);
        when(client.sendHeadRequest(storeWebClient, RESOURCE_PATH, Map.of())).thenReturn(Mono.empty());

        storeWeb.pingByResourcePath(STORE_URL + RESOURCE_PATH)
            .block();

        assertEquals(1, logAppender.list.size(), "Should log message.");
        ILoggingEvent loggingEvent = logAppender.list.getFirst();
        assertEquals(Level.TRACE, loggingEvent.getLevel(), "Should create TRACE log");
        assertEquals(
            "StoreWebImpl: Ping Resource at path 'http://localhost:8080/api/v1/organizations/079ad9d01e7c/documents/8c46667151db' has been passed.",
            loggingEvent.getFormattedMessage(),
            "Should log exact message");
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Log exact message if ping failed")
      void pingByResourcePath_logMessage() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(STORE_URL + RESOURCE_PATH));
        when(request.getMethod()).thenReturn(HttpMethod.HEAD);
        BadRequest badRequest = (BadRequest) WebClientResponseException.create(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.name(),
            HttpHeaders.EMPTY,
            objectMapper.writeValueAsBytes(new BadRequestDTO(List.of("Invalid id!"))),
            StandardCharsets.UTF_8,
            request);
        when(client.store()).thenReturn(storeWebClient);
        when(client.sendHeadRequest(storeWebClient, RESOURCE_PATH, Map.of())).thenReturn(Mono.error(badRequest));

        storeWeb.pingByResourcePath(STORE_URL + RESOURCE_PATH)
            .onErrorComplete()
            .block();

        assertEquals(1, logAppender.list.size(), "Should log message.");
        ILoggingEvent loggingEvent = logAppender.list.getFirst();
        assertEquals(Level.ERROR, loggingEvent.getLevel(), "Should create ERROR log");
        assertEquals(
            "StoreWebImpl: Ping Resource at path 'http://localhost:8080/api/v1/organizations/079ad9d01e7c/documents/8c46667151db' has been failed!\n400 BAD_REQUEST from HEAD http://localhost:8080/api/v1/organizations/079ad9d01e7c/documents/8c46667151db\n{\"code\":\"ValidationError\",\"message\":\"Invalid id!\"}",
            loggingEvent.getFormattedMessage(),
            "Should log exact message");
      }
    }

  }

  @Nested
  @DisplayName("getTokenByResourcePath")
  class GetTokenByResourcePath {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Fetch token for store resource using sotre WebClient and its relative URI")
      void getTokenByResourcePath_usesStoreClientAndBuildsToken() {
        try (MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {
          tokenFactory.when(() -> TokenFactory.build(tokenDTO)).thenReturn(Either.right(token));
          when(client.store()).thenReturn(storeWebClient);
          when(client.sendGetOneRequest(storeWebClient, RESOURCE_PATH + "/token", TokenResponseDTO.class, Map.of()))
              .thenReturn(Mono.just(tokenDTO));

          storeWeb.getTokenByResourcePath(STORE_URL + RESOURCE_PATH)
              .block();

          verify(
              client,
              description("GET request should be called with the store client and resource path in uri"))
              .sendGetOneRequest(storeWebClient, RESOURCE_PATH + "/token", TokenResponseDTO.class, Map.of());

        }
      }

      @Test
      @DisplayName("Fetch token for custom resource using its base URL with resource path")
      void getTokenByResourcePath_usesCustomClientAndBuildsToken() {

        try (MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {
          tokenFactory.when(() -> TokenFactory.build(tokenDTO)).thenReturn(Either.right(token));

          when(client.custom(STORE_URL_EXT + RESOURCE_PATH)).thenReturn(customWebClient);
          when(client.sendGetOneRequest(customWebClient, "/token", TokenResponseDTO.class, Map.of()))
              .thenReturn(Mono.just(tokenDTO));

          storeWeb.getTokenByResourcePath(STORE_URL_EXT + RESOURCE_PATH)
              .block();

          verify(
              client,
              description("Custom client has to be set with exact base url"))
              .custom(STORE_URL_EXT + RESOURCE_PATH);

          verify(
              client,
              description("GET request should be called with the exact custom client and 'token' in uri"))
              .sendGetOneRequest(customWebClient, "/token", TokenResponseDTO.class, Map.of());
        }
      }

      @Test
      @DisplayName("Build Token model from TokenResponseDTO")
      void getTokenByResourcePath_buildTokenModel() {
        try (MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {
          tokenFactory.when(() -> TokenFactory.build(tokenDTO)).thenReturn(Either.right(token));
          when(client.store()).thenReturn(storeWebClient);
          when(client.sendGetOneRequest(storeWebClient, RESOURCE_PATH + "/token", TokenResponseDTO.class, Map.of()))
              .thenReturn(Mono.just(tokenDTO));

          storeWeb.getTokenByResourcePath(STORE_URL + RESOURCE_PATH)
              .block();

          tokenFactory.verify(
              () -> TokenFactory.build(tokenDTO),
              description("Token model factory should be called"));
        }
      }

      @Test
      @DisplayName("Log exact message if request passed")
      void getTokenByResourcePath_logMessage() {
        try (MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {
          tokenFactory.when(() -> TokenFactory.build(tokenDTO)).thenReturn(Either.right(token));

          when(client.store()).thenReturn(storeWebClient);
          when(client.sendGetOneRequest(storeWebClient, RESOURCE_PATH + "/token", TokenResponseDTO.class, Map.of()))
              .thenReturn(Mono.just(tokenDTO));

          storeWeb.getTokenByResourcePath(STORE_URL + RESOURCE_PATH)
              .block();

          assertEquals(1, logAppender.list.size(), "Should log message.");
          ILoggingEvent loggingEvent = logAppender.list.getFirst();
          assertEquals(Level.TRACE, loggingEvent.getLevel(), "Should create TRACE log");
          assertEquals(
              "StoreWebImpl: Token for document at path 'http://localhost:8080/api/v1/organizations/079ad9d01e7c/documents/8c46667151db' has been fetched.",
              loggingEvent.getFormattedMessage(),
              "Should log exact message");
        }
      }

    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Log exact message if request failed")
      void getTokenByResourcePath_logMessage() {
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(STORE_URL + RESOURCE_PATH + "/token"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        BadRequest badRequest = (BadRequest) WebClientResponseException.create(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.name(),
            HttpHeaders.EMPTY,
            objectMapper.writeValueAsBytes(new BadRequestDTO(List.of("Invalid id!"))),
            StandardCharsets.UTF_8,
            request);
        when(client.store()).thenReturn(storeWebClient);
        when(client.sendGetOneRequest(storeWebClient, RESOURCE_PATH + "/token", TokenResponseDTO.class, Map.of())).thenReturn(Mono.error(badRequest));

        storeWeb.getTokenByResourcePath(STORE_URL + RESOURCE_PATH)
            .onErrorComplete()
            .block();

        assertEquals(1, logAppender.list.size(), "Should log message.");
        ILoggingEvent loggingEvent = logAppender.list.getFirst();
        assertEquals(Level.ERROR, loggingEvent.getLevel(), "Should create ERROR log");
        assertEquals(
            "StoreWebImpl: Token for document at path 'http://localhost:8080/api/v1/organizations/079ad9d01e7c/documents/8c46667151db' has not been fetched!\n400 BAD_REQUEST from GET http://localhost:8080/api/v1/organizations/079ad9d01e7c/documents/8c46667151db/token\n{\"code\":\"ValidationError\",\"message\":\"Invalid id!\"}",
            loggingEvent.getFormattedMessage(),
            "Should log exact message");
      }
    }
  }
}
