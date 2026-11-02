package org.commonprovenance.framework.store.web.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.commonprovenance.framework.store.controller.dto.error.BadRequestDTO;
import org.commonprovenance.framework.store.controller.dto.error.NotFoundDTO;
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
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreWebImpl UnitTest")
class StoreWebTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final String STORE_URL = "http://localhost:8080/api/v1/";
  private static final String RESOURCE_PATH = "organizations/079ad9d01e7c/documents/8c46667151db";

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
      @DisplayName("Ping store resource")
      void pingByResourcePath_shouldPass() {
        when(client.store()).thenReturn(storeWebClient);
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
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(requestUri));
        when(request.getMethod()).thenReturn(HttpMethod.HEAD);
        NotFound notFound = (NotFound) WebClientResponseException.create(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.name(),
            HttpHeaders.EMPTY,
            objectMapper.writeValueAsBytes(new NotFoundDTO("Resource not found!")),
            StandardCharsets.UTF_8,
            request);

        when(client.store()).thenReturn(storeWebClient);
        when(client.sendHeadRequest(storeWebClient, RESOURCE_PATH, Map.of())).thenReturn(Mono.error(notFound));

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
      @DisplayName("Should handle other as InternalApplicationException")
      void pingByResourcePath_HandleOtherExceptions() {
        String requestUri = STORE_URL + RESOURCE_PATH;
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(requestUri));
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

        StepVerifier.create(
            storeWeb.pingByResourcePath(requestUri),
            StepVerifierOptions.create().scenarioName("pinging a store resource should fail with InternalApplicationException"))
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
        String jwt = validJwt();
        when(client.store()).thenReturn(storeWebClient);
        when(client.sendGetOneRequest(storeWebClient, RESOURCE_PATH + "/token", TokenResponseDTO.class, Map.of()))
            .thenReturn(Mono.just(new TokenResponseDTO(jwt)));

        StepVerifier.create(
            storeWeb.getTokenByResourcePath(STORE_URL + RESOURCE_PATH),
            StepVerifierOptions.create().scenarioName("fetching a token resource should complete successfully"))
            .assertNext((Token token) -> {
              assertEquals(jwt, token.getJwt(), "should have exact jwt");
              token.getTokenTimestampAsLong()
                  .peek(ts -> assertEquals(1_700_000_000_000L / 1000, ts, "should have exact timestamp"))
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
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(requestUri));
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        NotFound notFound = (NotFound) WebClientResponseException.create(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.name(),
            HttpHeaders.EMPTY,
            objectMapper.writeValueAsBytes(new NotFoundDTO("Resource not found!")),
            StandardCharsets.UTF_8,
            request);

        when(client.store()).thenReturn(storeWebClient);
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
        String requestUri = STORE_URL + RESOURCE_PATH + "/token";
        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create(requestUri));
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

  private String validJwt() {
    SignedJWT signedJwt = new SignedJWT(
        new JWSHeader(JWSAlgorithm.HS256),
        new JWTClaimsSet.Builder().issueTime(new Date(1_700_000_000_000L)).build());
    try {

      signedJwt.sign(new MACSigner(new byte[32]));
      return signedJwt.serialize();
    } catch (JOSEException exception) {
      return null;
    }
  }
}
