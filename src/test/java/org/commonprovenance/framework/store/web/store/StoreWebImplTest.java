package org.commonprovenance.framework.store.web.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.store.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.web.store.impl.StoreWebImpl;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.ErrorResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreWebImpl UnitTest")
class StoreWebImplTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final String STORE_URL = "http://localhost:8080/api/v1/";
  private static final String RESOURCE_PATH = "organizations/079ad9d01e7c/documents/8c46667151db";

  @Mock
  private Client client;

  @Mock
  private WebClient storeWebClient;

  private StoreWeb storeWeb;

  @BeforeEach
  void setUp() {
    storeWeb = new StoreWebImpl(client);
    when(client.store()).thenReturn(storeWebClient);
    when(client.getStoreUrl()).thenReturn(STORE_URL);
  }

  @Nested
  @DisplayName("pingByResourcePath")
  class PingByResourcePath {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Ping store resource")
      void pingByResourcePath_shouldPass() {
        when(client.sendHeadRequest(storeWebClient, RESOURCE_PATH, Map.of())).thenReturn(Mono.empty());

        StepVerifier.create(
            storeWeb.pingByResourcePath(STORE_URL + RESOURCE_PATH),
            StepVerifierOptions.create().scenarioName("pinging a store resource should complete successfully"))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Should handle NotFound as NotFoundException")
      void pingByResourcePath_handleNotFound() {
        String requestUri = STORE_URL + RESOURCE_PATH;

        NotFound notFound = ((NotFound) buildHttpException(RESOURCE_PATH, HttpStatus.NOT_FOUND, "Not Found"));

        when(client.sendHeadRequest(storeWebClient, RESOURCE_PATH, Map.of())).thenReturn(Mono.error(notFound));

        StepVerifier.create(
            storeWeb.pingByResourcePath(requestUri),
            StepVerifierOptions.create().scenarioName("pinging a missing resource should fail with NotFoundException"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(NotFoundException.class, exception, "should be NotFoundException");
              assertEquals("Resource not found at: " + requestUri, exception.getMessage(), "should have exact error message");
            });
      }

      @Test
      @DisplayName("Should handle other as InternalApplicationException")
      void pingByResourcePath_HandleOtherExceptions() {
        String requestUri = STORE_URL + RESOURCE_PATH;

        BadRequest badRequest = ((BadRequest) buildHttpException(RESOURCE_PATH, HttpStatus.BAD_REQUEST, "Bad Request"));

        when(client.sendHeadRequest(storeWebClient, RESOURCE_PATH, Map.of())).thenReturn(Mono.error(badRequest));

        StepVerifier.create(
            storeWeb.pingByResourcePath(requestUri),
            StepVerifierOptions.create().scenarioName("pinging a resource should fail with InternalApplicationException"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(InternalApplicationException.class, exception, "should be InternalApplicationException");
              assertEquals("Ping Resource at path '" + requestUri + "' has been failed!", exception.getMessage(), "should have exact error message");
            });
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
      @DisplayName("Fetch token")
      void getTokenByResourcePath_shouldReturnToken() {
        String jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsInRydXN0ZWRQYXJ0eVVyaSI6ImxvY2FsaG9zdDo4MDIwIiwieDVjIjpbIk1JSUNNakNDQWRpZ0F3SUJBZ0lVU0xqNVk3UFhJUzEzcVBFUERkbElOQm5Rem9nd0NnWUlLb1pJemowRUF3SXdiVEVMTUFrR0ExVUVCaE1DUlZVeE9qQTRCZ05WQkFvTU1VUnBjM1J5YVdKMWRHVmtJRkJ5YjNabGJtRnVZMlVnUkdWdGJ5QkRaWEowYVdacFkyRjBaU0JCZFhSb2IzSnBkSGt4SWpBZ0JnTlZCQU1NR1VSUVJDQkRaWEowYVdacFkyRjBaU0JCZFhSb2IzSnBkSGt3SGhjTk1qUXhNVEUyTURJMU9UVXlXaGNOTXpReE1URTBNREkxT1RVeVdqQmRNUXN3Q1FZRFZRUUdFd0pEV2pFeU1EQUdBMVVFQ2d3cFJHbHpkSEpwWW5WMFpXUWdVSEp2ZG1WdVlXNWpaU0JFWlcxdklGUnlkWE4wWldRZ1VHRnlkSGt4R2pBWUJnTlZCQU1NRVVSUVJDQlVjblZ6ZEdWa0lGQmhjblI1TUZrd0V3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFK1Y4a1Q0amt2RVdtWDMwMUtBUzlla2xtblJOaTZnVTkrS0h4dVFwa1NPaE1UcTk2Q0JYRnBmb2tSZDd0NVZkclJ5MHVxWnN5U05wNWtXMGhuUU1KV2FObU1HUXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBREFPQmdOVkhROEJBZjhFQkFNQ0FZWXdIUVlEVlIwT0JCWUVGTUNuUFJqaVhva1Q3cXV3WlJCMTZBQWd6N2JuTUI4R0ExVWRJd1FZTUJhQUZDeUVLd2kxanZkUHFmaVUrTmRIL252aDdQWVpNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUUN5WnJVU2hWcXJvaERxZHpkT0ZtQXlGRHB3TUFPOEk2amFodmcxRlJBWllnSWdWaDRTMnRRbjEyWFlkZDVJU3NDcEFCc2g2WnJqU2lWWXJ0MlQxTzFuUXN3PSJdfQ.eyJzdWIiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL3YxL29yZ2FuaXphdGlvbnMvNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjL2RvY3VtZW50cy9kYzhlZmVkMC0wMDM1LTQwMjktOTA2NS04YzQ2NjY3MTUxZGIiLCJoYXNoX2FsZyI6IlNIQTI1NiIsImRvY19kaWdlc3QiOiI1Y2IzNzY2ZGZkZjhlMTVhNTVmMjA5YThhYTBhYmE5MTg3M2Y2MWU2Yzk1MjI2NzlkNzc5NDRiN2NkOGRkMjc5Iiwib3JnX2lkIjoiNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjIiwiaXNzIjoiVHJ1c3RlZFBhcnR5IiwiaWF0IjoxNzkyNjk1OTk1LCJkb2NfaWF0IjoxNzkyNjk1OTk1fQ.4sKu7-HPkKDSvj9BIPZ-1Dc4bZ-GRbp5aIWjlzXxZIUfj0GlTYkhlJ_TovNRtBw0lIZxOTx4jMFtFWIMfSl6ZQ";
        when(client.sendGetOneRequest(storeWebClient, RESOURCE_PATH + "/token", TokenResponseDTO.class, Map.of()))
            .thenReturn(Mono.just(new TokenResponseDTO(jwt)));

        StepVerifier.create(
            storeWeb.getTokenByResourcePath(STORE_URL + RESOURCE_PATH),
            StepVerifierOptions.create().scenarioName("fetching a token resource should complete successfully"))
            .assertNext((Token token) -> {
              assertEquals(jwt, token.getJwt(), "should have exact jwt");
              token.getTokenTimestampAsLong()
                  .peek(ts -> assertEquals(1_792_695_995L, ts, "should have exact timestamp"))
                  .peekLeft(ex -> fail("Left side has not been expected: " + ex.getMessage()));
              assertTrue(token.getTrustedParty().isEmpty(), "should not be set");

            })
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Should handle NotFound as NotFoundException")
      void getTokenByResourcePath_handleNotFound() {
        String requestUri = STORE_URL + RESOURCE_PATH + "/token";

        NotFound notFound = ((NotFound) buildHttpException(RESOURCE_PATH + "/token", HttpStatus.NOT_FOUND, "Not Found"));

        when(client.sendGetOneRequest(storeWebClient, RESOURCE_PATH + "/token", TokenResponseDTO.class, Map.of())).thenReturn(Mono.error(notFound));

        StepVerifier.create(
            storeWeb.getTokenByResourcePath(STORE_URL + RESOURCE_PATH),
            StepVerifierOptions.create().scenarioName("fetching a missing token resource should fail with NotFoundException"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(NotFoundException.class, exception, "should be NotFoundException");
              assertEquals("Resource not found at: " + requestUri, exception.getMessage(), "should have exact error message");
            });

      }

      @Test
      @DisplayName("Should handle other as InternalApplicationException")
      void getTokenByResourcePath_HandleOtherExceptions() {
        BadRequest badRequest = ((BadRequest) buildHttpException(RESOURCE_PATH + "/token", HttpStatus.BAD_REQUEST, "BadRequest"));

        when(client.sendGetOneRequest(storeWebClient, RESOURCE_PATH + "/token", TokenResponseDTO.class, Map.of())).thenReturn(Mono.error(badRequest));

        StepVerifier.create(
            storeWeb.getTokenByResourcePath(STORE_URL + RESOURCE_PATH),
            StepVerifierOptions.create().scenarioName("fetching a token resource should fail with InternalApplicationException"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(InternalApplicationException.class, exception, "should be InternalApplicationException");
              assertEquals("Token for document at path '" + STORE_URL + RESOURCE_PATH + "' has not been fetched!", exception.getMessage(),
                  "should have exact error message");
            });
      }
    }
  }

  private WebClientResponseException buildHttpException(String requestUri, HttpStatus status, String message) {
    ErrorResponseDTO response = new ErrorResponseDTO(LocalDateTime.of(2026, 11, 16, 0, 0),
        status,
        message,
        "/api/v1" + requestUri);

    HttpRequest request = mock(HttpRequest.class);
    when(request.getURI()).thenReturn(URI.create(STORE_URL + requestUri));
    when(request.getMethod()).thenReturn(HttpMethod.POST);

    WebClientResponseException unauthorized = WebClientResponseException.create(
        status.value(),
        status.name(),
        HttpHeaders.EMPTY,
        objectMapper.writeValueAsBytes(response),
        StandardCharsets.UTF_8,
        request);
    unauthorized.setBodyDecodeFunction(targetType -> objectMapper.readValue(
        unauthorized.getResponseBodyAsByteArray(),
        objectMapper.constructType(targetType.getType())));

    return unauthorized;
  }
}
