package org.commonprovenance.framework.store.web.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Date;
import java.util.Map;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.store.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.web.store.impl.StoreWebImpl;
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
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import org.springframework.web.reactive.function.client.WebClientResponseException.Conflict;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreWebImpl UnitTest")
class StoreWebTest {

  private static final String STORE_URL = "http://localhost:8080/api/v1/";
  private static final String STORE_URL_EXT = "http://localhost:80/api/v1/";

  private static final String STORE_RESOURCE_PATH_EXISTS = "organizations/079ad9d01e7c/documents/8c46667151db";
  private static final String STORE_RESOURCE_PATH_NOT_FOUND = "organizations/6fb292aa/documents/dc8efed0";

  @Mock
  private Client client;

  @Mock
  private WebClient storeWebClient;

  @Mock
  private WebClient customWebClient;

  private StoreWebImpl storeWeb;

  @BeforeEach
  void setUp() {
    storeWeb = new StoreWebImpl(client);
    when(client.getStoreUrl()).thenReturn(STORE_URL);
  }

  @Nested
  @DisplayName("pingByResourcePath")
  class PingByResourcePath {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Ping store resource using its relative URI")
      void pingByResourcePath_usesStoreClientForStoreResource() {
        when(client.store()).thenReturn(storeWebClient);
        when(client.sendHeadRequest(storeWebClient, STORE_RESOURCE_PATH_EXISTS, Map.of())).thenReturn(Mono.empty());

        StepVerifier.create(
            storeWeb.pingByResourcePath(STORE_URL + STORE_RESOURCE_PATH_EXISTS),
            StepVerifierOptions.create().scenarioName("pinging a store resource should complete successfully"))
            .verifyComplete();

        verify(client, description("HEAD request should be called with the store client and resource path"))
            .sendHeadRequest(storeWebClient, STORE_RESOURCE_PATH_EXISTS, Map.of());
      }

      @Test
      @DisplayName("Ping custom resource using its base URL")
      void pingByResourcePath_usesCustomClientForExternalResource() {
        when(client.custom(anyString())).thenReturn(customWebClient);
        when(client.sendHeadRequest(customWebClient, "", Map.of())).thenReturn(Mono.empty());

        StepVerifier.create(
            storeWeb.pingByResourcePath(STORE_URL_EXT + STORE_RESOURCE_PATH_EXISTS),
            StepVerifierOptions.create().scenarioName("pinging a store resource should complete successfully"))
            .verifyComplete();

        verify(client, description("HEAD request should be called with the exact custom client and empty resource path"))
            .sendHeadRequest(customWebClient, "", Map.of());
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("ErrorPath - Should handle NotFound as NotFoundException")
      void pingByResourcePath_HandleNotFound() {
        String requestUri = STORE_URL + STORE_RESOURCE_PATH_NOT_FOUND;
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(requestUri));
        when(request.getMethod()).thenReturn(HttpMethod.HEAD);
        NotFound notFound = (NotFound) WebClientResponseException.create(
            HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name(), HttpHeaders.EMPTY, new byte[0], null, request);

        when(client.store()).thenReturn(storeWebClient);
        when(client.sendHeadRequest(storeWebClient, STORE_RESOURCE_PATH_NOT_FOUND, Map.of())).thenReturn(Mono.error(notFound));

        StepVerifier.create(
            storeWeb.pingByResourcePath(requestUri),
            StepVerifierOptions.create().scenarioName("pinging a missing store resource should fail with NotFoundException"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(NotFoundException.class, exception, "should be NotFoundException");
              assertEquals("Resource not found at: " + requestUri, exception.getMessage(), "should have exact error message");
            });
      }

      @Test
      @DisplayName("ErrorPath - Should handle other as InternalApplicationException")
      void pingByResourcePath_HandleOtherExceptions() {
        String requestUri = STORE_URL + STORE_RESOURCE_PATH_EXISTS;
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(requestUri));
        when(request.getMethod()).thenReturn(HttpMethod.HEAD);
        Conflict conflict = (Conflict) WebClientResponseException.create(
            HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.name(), HttpHeaders.EMPTY, new byte[0], null, request);

        when(client.store()).thenReturn(storeWebClient);
        when(client.sendHeadRequest(storeWebClient, STORE_RESOURCE_PATH_EXISTS, Map.of())).thenReturn(Mono.error(conflict));

        StepVerifier.create(
            storeWeb.pingByResourcePath(requestUri),
            StepVerifierOptions.create().scenarioName("pinging a missing store resource should fail with NotFoundException"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(InternalApplicationException.class, exception, "should be NotFoundException");
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
    }
  }
  // @Test
  // @DisplayName("Fetch token for store resource using its relative URI")
  // void getTokenByResourcePath_usesStoreClientAndBuildsToken() throws Exception {
  // String jwt = validJwt();
  // when(client.store()).thenReturn(storeWebClient);
  // when(client.sendGetOneRequest(storeWebClient, "/document-123/token", TokenResponseDTO.class, Map.of()))
  // .thenReturn(Mono.just(new TokenResponseDTO(jwt)));

  // StepVerifier.create(storeWeb.getTokenByResourcePath(STORE_RESOURCE_PATH))
  // .assertNext(token -> assertEquals(jwt, token.getJwt()))
  // .verifyComplete();

  // verify(client).sendGetOneRequest(storeWebClient, "/document-123/token", TokenResponseDTO.class, Map.of());
  // }

  // @Test
  // @DisplayName("Fetch token for custom resource using its base URL")
  // void getTokenByResourcePath_usesCustomClientAndBuildsToken() throws Exception {
  // String jwt = validJwt();
  // when(client.custom(CUSTOM_RESOURCE_PATH)).thenReturn(customWebClient);
  // when(client.sendGetOneRequest(customWebClient, "/token", TokenResponseDTO.class, Map.of()))
  // .thenReturn(Mono.just(new TokenResponseDTO(jwt)));

  // StepVerifier.create(storeWeb.getTokenByResourcePath(CUSTOM_RESOURCE_PATH))
  // .assertNext((Token token) -> assertEquals(jwt, token.getJwt()))
  // .verifyComplete();

  // verify(client).sendGetOneRequest(customWebClient, "/token", TokenResponseDTO.class, Map.of());
  // }

  // private static String validJwt() throws Exception {
  // SignedJWT signedJwt = new SignedJWT(
  // new JWSHeader(JWSAlgorithm.HS256),
  // new JWTClaimsSet.Builder().issueTime(new Date(1_700_000_000_000L)).build());
  // signedJwt.sign(new MACSigner(new byte[32]));
  // return signedJwt.serialize();
  // }
}
