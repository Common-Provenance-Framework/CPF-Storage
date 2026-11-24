
package org.commonprovenance.framework.store.web.trustedParty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.MetaDocument;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.IssueTokenFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.VerifySignatureFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.ErrorResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TrustedPartyResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.impl.TrustedPartyWebImpl;
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
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import org.springframework.web.reactive.function.client.WebClientResponseException.Conflict;

import io.vavr.control.Either;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrustedPartyWeb UnitTest")
class TrustedPartyWebImplTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private String TRUSTED_PARTY_URL = "http://localhost:8020/api/v1";
  private String TRUSTED_PARTY_ID = "TrustedParty";
  private String TRUSTED_PARTY_CERT = """
      -----BEGIN CERTIFICATE-----
      MIICMjCCAdigAwIBAgIUSLj5Y7PXIS13qPEPDdlINBnQzogwCgYIKoZIzj0EAwIw
      bTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug
      RGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp
      Y2F0ZSBBdXRob3JpdHkwHhcNMjQxMTE2MDI1OTUyWhcNMzQxMTE0MDI1OTUyWjBd
      MQswCQYDVQQGEwJDWjEyMDAGA1UECgwpRGlzdHJpYnV0ZWQgUHJvdmVuYW5jZSBE
      ZW1vIFRydXN0ZWQgUGFydHkxGjAYBgNVBAMMEURQRCBUcnVzdGVkIFBhcnR5MFkw
      EwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+V8kT4jkvEWmX301KAS9eklmnRNi6gU9
      +KHxuQpkSOhMTq96CBXFpfokRd7t5VdrRy0uqZsySNp5kW0hnQMJWaNmMGQwEgYD
      VR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0OBBYEFMCnPRji
      XokT7quwZRB16AAgz7bnMB8GA1UdIwQYMBaAFCyEKwi1jvdPqfiU+NdH/nvh7PYZ
      MAoGCCqGSM49BAMCA0gAMEUCIQCyZrUShVqrohDqdzdOFmAyFDpwMAO8I6jahvg1
      FRAZYgIgVh4S2tQn12XYdd5ISsCpABsh6ZrjSiVYrt2T1O1nQsw=
      -----END CERTIFICATE-----
      """;
  private String ORGANIZATION_CERT = """
      -----BEGIN CERTIFICATE-----
      MIIB8DCCAZWgAwIBAgIUajAZAZAMKJqrnd/5kF9gBsWqZncwCgYIKoZIzj0EAwIw
      LjELMAkGA1UEBhMCQ1oxDDAKBgNVBAoMA0NQRjERMA8GA1UEAwwIY3BmLWludDIw
      HhcNMjYxMTE2MTIxMTI0WhcNMjkwMjE4MTIxMTI0WjBKMQswCQYDVQQGEwJDWjEM
      MAoGA1UECgwDQ1BGMS0wKwYDVQQDDCQ2ZmIyOTJhYS1lZTM4LTQ4YWUtOTk4Zi0w
      NzlhZDlkMDFlN2UwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAASD1L1cy4FP8NsI
      PBZC18iNZugiGZRRhO7ZgTzDyD0WOYucsiS6CUBtiIawhPosZXLOvtEkbCmvGdFD
      QjGdyjEmo3UwczAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIHgDATBgNVHSUE
      DDAKBggrBgEFBQcDAjAdBgNVHQ4EFgQUrz/Q+NtvSPMnmskTD7KzxLLRoXkwHwYD
      VR0jBBgwFoAUWuNDU3VuQ3L53GQJN0wHo5snWNUwCgYIKoZIzj0EAwIDSQAwRgIh
      AIHFzhwQPGF0OtnrRkYXfDHtNz1KyY7wGwBbSViUeTHnAiEArN4DY/sV4y7u5std
      933tUOgAOm0oPQyh4UwrfwSvUlU=
      -----END CERTIFICATE-----
      """;
  @Mock
  private Client client;

  @Mock
  private WebClient webClient;

  private TrustedPartyWeb trustedPartyWeb;

  @BeforeEach
  void setUp() {
    trustedPartyWeb = new TrustedPartyWebImpl(client);
    when(client.trustedParty(Optional.empty())).thenReturn(webClient);

    when(client.getDefaultTrustedPartyUrl()).thenReturn(TRUSTED_PARTY_URL);
  }

  @Nested
  @DisplayName("getTrustedParty")
  class GetTrustedParty {
    private String INFO_URI = "/info";

    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Get TrustedParty info object")
      void getTrustedParty_returnsTPInfo() {
        when(client.sendGetOneRequest(webClient, INFO_URI, TrustedPartyResponseDTO.class, Map.of()))
            .thenReturn(Mono.just(new TrustedPartyResponseDTO(TRUSTED_PARTY_ID, TRUSTED_PARTY_CERT)));

        StepVerifier.create(
            trustedPartyWeb.getTrustedParty(Optional.empty()),
            StepVerifierOptions.create().scenarioName("should get TrustedParty info object"))
            .assertNext((TrustedParty tp) -> {
              assertEquals(TRUSTED_PARTY_ID, tp.getId(), "should have exact id");
              assertEquals(TRUSTED_PARTY_CERT, tp.getClientCertificate(), "should have exact client certificate");
              assertEquals(Optional.empty(), tp.getUrl(), "should have empty url when default");
              assertEquals(true, tp.getIsChecked(), "should be checked");
              assertEquals(true, tp.getIsValid(), "should be valid");
              assertEquals(true, tp.getIsDefault(), "should be default");
            })
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Should handle Exception")
      void getTrustedParty_handleException() {

        BadRequest badRequest = ((BadRequest) buildHttpException(INFO_URI, HttpStatus.BAD_REQUEST, "Bad Request"));

        when(client.sendGetOneRequest(webClient, "/info", TrustedPartyResponseDTO.class, Map.of()))
            .thenReturn(Mono.error(() -> badRequest));

        StepVerifier.create(
            trustedPartyWeb.getTrustedParty(Optional.empty()),
            StepVerifierOptions.create().scenarioName("should fail with exact InternalApplicationException if http request fail"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(InternalApplicationException.class, exception, "should be InternalApplicationException");
              assertEquals(
                  "Trusted Party info object has not been fetched from url: 'http://localhost:8020/api/v1'!",
                  exception.getMessage(),
                  "should have exact error message");
            });
      }
    }
  }

  @Nested
  @DisplayName("issueGraphToken")
  class IssueGraphToken {
    private String ISSUE_TOKEN_URI = "/issueToken";
    private String ORGANIZATION_ID = "6fb292aa-ee38-48ae-998f-079ad9d01e7e";

    private String JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsInRydXN0ZWRQYXJ0eVVyaSI6ImxvY2FsaG9zdDo4MDIwIiwieDVjIjpbIk1JSUNNakNDQWRpZ0F3SUJBZ0lVU0xqNVk3UFhJUzEzcVBFUERkbElOQm5Rem9nd0NnWUlLb1pJemowRUF3SXdiVEVMTUFrR0ExVUVCaE1DUlZVeE9qQTRCZ05WQkFvTU1VUnBjM1J5YVdKMWRHVmtJRkJ5YjNabGJtRnVZMlVnUkdWdGJ5QkRaWEowYVdacFkyRjBaU0JCZFhSb2IzSnBkSGt4SWpBZ0JnTlZCQU1NR1VSUVJDQkRaWEowYVdacFkyRjBaU0JCZFhSb2IzSnBkSGt3SGhjTk1qUXhNVEUyTURJMU9UVXlXaGNOTXpReE1URTBNREkxT1RVeVdqQmRNUXN3Q1FZRFZRUUdFd0pEV2pFeU1EQUdBMVVFQ2d3cFJHbHpkSEpwWW5WMFpXUWdVSEp2ZG1WdVlXNWpaU0JFWlcxdklGUnlkWE4wWldRZ1VHRnlkSGt4R2pBWUJnTlZCQU1NRVVSUVJDQlVjblZ6ZEdWa0lGQmhjblI1TUZrd0V3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFK1Y4a1Q0amt2RVdtWDMwMUtBUzlla2xtblJOaTZnVTkrS0h4dVFwa1NPaE1UcTk2Q0JYRnBmb2tSZDd0NVZkclJ5MHVxWnN5U05wNWtXMGhuUU1KV2FObU1HUXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBREFPQmdOVkhROEJBZjhFQkFNQ0FZWXdIUVlEVlIwT0JCWUVGTUNuUFJqaVhva1Q3cXV3WlJCMTZBQWd6N2JuTUI4R0ExVWRJd1FZTUJhQUZDeUVLd2kxanZkUHFmaVUrTmRIL252aDdQWVpNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUUN5WnJVU2hWcXJvaERxZHpkT0ZtQXlGRHB3TUFPOEk2amFodmcxRlJBWllnSWdWaDRTMnRRbjEyWFlkZDVJU3NDcEFCc2g2WnJqU2lWWXJ0MlQxTzFuUXN3PSJdfQ.eyJzdWIiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL3YxL29yZ2FuaXphdGlvbnMvNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjL2RvY3VtZW50cy9kYzhlZmVkMC0wMDM1LTQwMjktOTA2NS04YzQ2NjY3MTUxZGIiLCJoYXNoX2FsZyI6IlNIQTI1NiIsImRvY19kaWdlc3QiOiI1Y2IzNzY2ZGZkZjhlMTVhNTVmMjA5YThhYTBhYmE5MTg3M2Y2MWU2Yzk1MjI2NzlkNzc5NDRiN2NkOGRkMjc5Iiwib3JnX2lkIjoiNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjIiwiaXNzIjoiVHJ1c3RlZFBhcnR5IiwiaWF0IjoxNzkyNjk1OTk1LCJkb2NfaWF0IjoxNzkyNjk1OTk1fQ.4sKu7-HPkKDSvj9BIPZ-1Dc4bZ-GRbp5aIWjlzXxZIUfj0GlTYkhlJ_TovNRtBw0lIZxOTx4jMFtFWIMfSl6ZQ";

    private String GRAPH = "ewogICJidW5kbGUiOiB7CiAgICAic3RvcmFnZTpkYzhlZmVkMC0wMDM1LTQwMjktOTA2NS04YzQ2NjY3MTUxZGIiOiB7CiAgICAgICJwcmVmaXgiOiB7CiAgICAgICAgInNjaGVtYSI6ICJodHRwczovL3NjaGVtYS5vcmcvIiwKICAgICAgICAiY3BtIjogImh0dHBzOi8vd3d3LmNvbW1vbnByb3ZlbmFuY2Vtb2RlbC5vcmcvY3BtLW5hbWVzcGFjZS12MS0wLyIsCiAgICAgICAgImJsYW5rIjogImh0dHBzOi8vb3BlbnByb3ZlbmFuY2Uub3JnL2JsYW5rLyIsCiAgICAgICAgInhzZCI6ICJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSMiLAogICAgICAgICJvcmNpZCI6ICJodHRwczovL29yY2lkLm9yZy8iLAogICAgICAgICJvYm9saWJyYXJ5IjogImh0dHA6Ly9wdXJsLm9ib2xpYnJhcnkub3JnL29iby8iLAogIC..";

    private String SIGNATURE = "MEYCIQC9g58Sk51VWk82yMt9N2BDHnW/a+Hf7y3+RonC5/54YAIhAN1dHjSbyJHRXdKBRvudW7Ix+q71SDewwlLK2njHCdEe";

    private Document DOCUMENT = new Document(GRAPH, GraphFormat.JSON);
    private TrustedParty TRUSTED_PARTY = new TrustedParty(TRUSTED_PARTY_ID, TRUSTED_PARTY_CERT, null, true);
    private Organization ORGANIZATION = new Organization(ORGANIZATION_ID, ORGANIZATION_CERT, List.of(), TRUSTED_PARTY, DOCUMENT);

    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Issue Graph Token")
      void issueGraphToken_issueTokenGraph() {
        when(client.sendPostRequest(webClient, ISSUE_TOKEN_URI, TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> Mono.just(new TokenResponseDTO(JWT)));

        StepVerifier.create(
            trustedPartyWeb.issueGraphToken(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should issue graph token"))
            .assertNext((Token token) -> {
              assertEquals(JWT, token.getJwt(), "should have exact jwt token");
              assertEquals(Optional.empty(), token.getTrustedParty(), "should not be loaded");
              assertEquals(1_792_695_995, token.getCreatedOn(), "should have exact timestamp");
            })
            .verifyComplete();
      }

      @Test
      @DisplayName("Issue Subgraph Token")
      void issueGraphToken_issueTokenSubgraph() {

        when(client.sendPostRequest(webClient, ISSUE_TOKEN_URI, TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> Mono.just(new TokenResponseDTO(JWT)));

        StepVerifier.create(
            trustedPartyWeb.issueGraphToken(GraphType.BACKBONE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should issue graph token"))
            .assertNext((Token token) -> {
              assertEquals(JWT, token.getJwt(), "should have exact jwt token");
              assertEquals(Optional.empty(), token.getTrustedParty(), "should not be loaded");
              assertEquals(1_792_695_995, token.getCreatedOn(), "should have exact timestamp");
            })
            .verifyComplete();
      }

      @Test
      @DisplayName("Issue MetaDocument Token")
      void issueGraphToken_issueTokenMeta() {
        String META_GRAP = "ewogICJidW5kbGUiOiB7CiAgICAic3RvcmFnZTpkYzhlZmVkMC0wMDM1LTQwMjktOTA2NS0...";
        MetaDocument meta = mock(MetaDocument.class);
        when(meta.getOrganizationIdentifier()).thenReturn(Either.right(ORGANIZATION_ID));
        when(meta.getB64Graph()).thenReturn(Either.right(META_GRAP));
        when(meta.getTrustedParty(TRUSTED_PARTY_URL)).thenReturn(TRUSTED_PARTY);

        when(client.getDefaultTrustedPartyUrl()).thenReturn(TRUSTED_PARTY_URL);
        when(client.sendPostRequest(webClient, ISSUE_TOKEN_URI, TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> Mono.just(new TokenResponseDTO(JWT)));

        StepVerifier.create(
            trustedPartyWeb.issueGraphToken(meta),
            StepVerifierOptions.create().scenarioName("should issue graph token"))
            .assertNext((Token token) -> {
              assertEquals(JWT, token.getJwt(), "should have exact jwt token");
              assertEquals(Optional.empty(), token.getTrustedParty(), "should not be loaded");
              assertEquals(1_792_695_995, token.getCreatedOn(), "should have exact timestamp");
            })
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {

      @Test
      @DisplayName("Should handle Unauthorized")
      void issueGraphToken_handleUnauthorized() {
        Unauthorized unauthorized = ((Unauthorized) buildHttpException(ISSUE_TOKEN_URI, HttpStatus.UNAUTHORIZED, "Unauthorized"));

        when(client.sendPostRequest(webClient, ISSUE_TOKEN_URI, TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> Mono.error(() -> unauthorized));

        StepVerifier.create(
            trustedPartyWeb.issueGraphToken(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should fail with exact BadRequestException if http request fail with Unauthorized"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(BadRequestException.class, exception, "should be BadRequestException");
              assertEquals(
                  "TrustedParty responded with status '401 UNAUTHORIZED' and message 'Unauthorized'!",
                  exception.getMessage(),
                  "should have exact error message");
            });
      }

      @Test
      @DisplayName("Should handle BadRequest")
      void issueGraphToken_handleNotBadRequest() {
        BadRequest badRequest = ((BadRequest) buildHttpException(ISSUE_TOKEN_URI, HttpStatus.BAD_REQUEST, "Bad Request"));

        when(client.sendPostRequest(webClient, ISSUE_TOKEN_URI, TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> Mono.error(() -> badRequest));

        StepVerifier.create(
            trustedPartyWeb.issueGraphToken(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should fail with exact BadRequestException if http request fail with BadRequest"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(BadRequestException.class, exception, "should be BadRequestException");
              assertEquals(
                  "TrustedParty responded with status '400 BAD_REQUEST' and message 'Bad Request'!",
                  exception.getMessage(),
                  "should have exact error message");
            });
      }

      @Test
      @DisplayName("Should handle NotFound")
      void issueGraphToken_handleNotFound() {
        NotFound notFound = ((NotFound) buildHttpException(ISSUE_TOKEN_URI, HttpStatus.NOT_FOUND, "Not Found"));

        when(client.sendPostRequest(webClient, ISSUE_TOKEN_URI, TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> Mono.error(() -> notFound));

        StepVerifier.create(
            trustedPartyWeb.issueGraphToken(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should fail with exact BadRequestException if http request fail with NotFound"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(BadRequestException.class, exception, "should be BadRequestException");
              assertEquals(
                  "TrustedParty responded with status '404 NOT_FOUND' and message 'Not Found'!",
                  exception.getMessage(),
                  "should have exact error message");
            });
      }

      @Test
      @DisplayName("Should handle Conflict")
      void issueGraphToken_handleConflict() {
        Conflict conflict = ((Conflict) buildHttpException(ISSUE_TOKEN_URI, HttpStatus.CONFLICT, "Conflict"));

        when(client.sendPostRequest(webClient, ISSUE_TOKEN_URI, TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> Mono.error(() -> conflict));

        StepVerifier.create(
            trustedPartyWeb.issueGraphToken(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should fail with exact BadRequestException if http request fail with Conflict"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(BadRequestException.class, exception, "should be BadRequestException");
              assertEquals(
                  "TrustedParty responded with status '409 CONFLICT' and message 'Conflict'!",
                  exception.getMessage(),
                  "should have exact error message");
            });
      }

      @Test
      @DisplayName("Should handle Unexpect exceptions as InternalApplicationException")
      void issueGraphToken_handleUnexpect() {

        Throwable unexpect = new Throwable("Some exception");
        when(client.sendPostRequest(webClient, ISSUE_TOKEN_URI, TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> Mono.error(() -> unexpect));

        StepVerifier.create(
            trustedPartyWeb.issueGraphToken(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should fail with exact InternalApplicationException if unexpected exception"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(InternalApplicationException.class, exception, "should be InternalApplicationException");
              assertEquals(
                  "Token has not been issued by TrustedParty!",
                  exception.getMessage(),
                  "should have exact error message");

              Throwable cause = exception.getCause();
              assertNotNull(cause, "should have cause exception");
              assertEquals("Some exception", cause.getMessage(), "should be exact exception");
            });
      }
    }
  }

  @Nested
  @DisplayName("verifySignature")
  class VerifySignature {
    private String VERIFY_SIG_URI = "/verifySignature";
    private String ORGANIZATION_ID = "6fb292aa-ee38-48ae-998f-079ad9d01e7e";

    private String GRAPH = "ewogICJidW5kbGUiOiB7CiAgICAic3RvcmFnZTpkYzhlZmVkMC0wMDM1LTQwMjktOTA2NS04YzQ2NjY3MTUxZGIiOiB7CiAgICAgICJwcmVmaXgiOiB7CiAgICAgICAgInNjaGVtYSI6ICJodHRwczovL3NjaGVtYS5vcmcvIiwKICAgICAgICAiY3BtIjogImh0dHBzOi8vd3d3LmNvbW1vbnByb3ZlbmFuY2Vtb2RlbC5vcmcvY3BtLW5hbWVzcGFjZS12MS0wLyIsCiAgICAgICAgImJsYW5rIjogImh0dHBzOi8vb3BlbnByb3ZlbmFuY2Uub3JnL2JsYW5rLyIsCiAgICAgICAgInhzZCI6ICJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSMiLAogICAgICAgICJvcmNpZCI6ICJodHRwczovL29yY2lkLm9yZy8iLAogICAgICAgICJvYm9saWJyYXJ5IjogImh0dHA6Ly9wdXJsLm9ib2xpYnJhcnkub3JnL29iby8iLAogIC..";

    private String SIGNATURE = "MEYCIQC9g58Sk51VWk82yMt9N2BDHnW/a+Hf7y3+RonC5/54YAIhAN1dHjSbyJHRXdKBRvudW7Ix+q71SDewwlLK2njHCdEe";

    private Document DOCUMENT = new Document(GRAPH, GraphFormat.JSON);
    private TrustedParty TRUSTED_PARTY = new TrustedParty(TRUSTED_PARTY_ID, TRUSTED_PARTY_CERT, null, true);
    private Organization ORGANIZATION = new Organization(ORGANIZATION_ID, ORGANIZATION_CERT, List.of(), TRUSTED_PARTY, DOCUMENT);

    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Verifiy Signature - valid")
      void verifySignature_passTrue() {
        when(client.sendPostRequest(webClient, VERIFY_SIG_URI, Boolean.class, Map.of()))
            .thenAnswer(_ -> (Function<VerifySignatureFormDTO, Mono<Boolean>>) form -> Mono.just(true));

        StepVerifier.create(
            trustedPartyWeb.verifySignature(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should be valid signature"))
            .assertNext((Boolean result) -> {
              assertEquals(true, result, "should be true, if signature is valid");
            })
            .verifyComplete();
      }

      @Test
      @DisplayName("Verifiy Signature - invalid")
      void verifySignature_passFalse() {
        Unauthorized unauthorized = ((Unauthorized) buildHttpException(VERIFY_SIG_URI, HttpStatus.UNAUTHORIZED, "Unauthorized"));

        when(client.sendPostRequest(webClient, VERIFY_SIG_URI, Boolean.class, Map.of()))
            .thenAnswer(_ -> (Function<VerifySignatureFormDTO, Mono<Boolean>>) form -> Mono.error(() -> unauthorized));

        StepVerifier.create(
            trustedPartyWeb.verifySignature(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should be invalid signature"))
            .assertNext((Boolean result) -> {
              assertEquals(false, result, "should be false, if signature is invalid");
            })
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {

      @Test
      @DisplayName("Should handle BadRequest")
      void issueGraphToken_handleNotBadRequest() {
        BadRequest badRequest = ((BadRequest) buildHttpException(VERIFY_SIG_URI, HttpStatus.BAD_REQUEST, "Bad Request"));

        when(client.sendPostRequest(webClient, VERIFY_SIG_URI, Boolean.class, Map.of()))
            .thenAnswer(_ -> (Function<VerifySignatureFormDTO, Mono<Boolean>>) form -> Mono.error(() -> badRequest));

        StepVerifier.create(
            trustedPartyWeb.verifySignature(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should fail with exact BadRequestException if http request fail with BadRequest"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(BadRequestException.class, exception, "should be BadRequestException");
              assertEquals(
                  "TrustedParty responded with status '400 BAD_REQUEST' and message 'Bad Request'!",
                  exception.getMessage(),
                  "should have exact error message");
            });
      }

      @Test
      @DisplayName("Should handle NotFound")
      void issueGraphToken_handleNotFound() {
        NotFound notFound = ((NotFound) buildHttpException(VERIFY_SIG_URI, HttpStatus.NOT_FOUND, "Not Found"));

        when(client.sendPostRequest(webClient, VERIFY_SIG_URI, Boolean.class, Map.of()))
            .thenAnswer(_ -> (Function<VerifySignatureFormDTO, Mono<Boolean>>) form -> Mono.error(() -> notFound));

        StepVerifier.create(
            trustedPartyWeb.verifySignature(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should fail with exact BadRequestException if http request fail with NotFound"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(BadRequestException.class, exception, "should be BadRequestException");
              assertEquals(
                  "TrustedParty responded with status '404 NOT_FOUND' and message 'Not Found'!",
                  exception.getMessage(),
                  "should have exact error message");
            });
      }

      @Test
      @DisplayName("Should handle Unexpect exceptions as InternalApplicationException")
      void issueGraphToken_handleUnexpect() {

        Throwable unexpect = new Throwable("Some exception");
        when(client.sendPostRequest(webClient, VERIFY_SIG_URI, Boolean.class, Map.of()))
            .thenAnswer(_ -> (Function<VerifySignatureFormDTO, Mono<Boolean>>) form -> Mono.error(() -> unexpect));

        StepVerifier.create(
            trustedPartyWeb.verifySignature(SIGNATURE).apply(ORGANIZATION),
            StepVerifierOptions.create().scenarioName("should fail with exact InternalApplicationException if unexpected exception"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(InternalApplicationException.class, exception, "should be InternalApplicationException");
              assertEquals(
                  "Signature has not been verified by TrustedParty at URL 'http://localhost:8020/api/v1'!",
                  exception.getMessage(),
                  "should have exact error message");

              Throwable cause = exception.getCause();
              assertNotNull(cause, "should have cause exception");
              assertEquals("Some exception", cause.getMessage(), "should be exact exception");
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
    when(request.getURI()).thenReturn(URI.create(TRUSTED_PARTY_URL + requestUri));
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
