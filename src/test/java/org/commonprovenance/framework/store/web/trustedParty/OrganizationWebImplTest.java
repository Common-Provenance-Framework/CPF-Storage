
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
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.RegisterOrganizationFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.UpdateOrganizationFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.ErrorResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.impl.OrganizationWebImpl;
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
import org.springframework.web.reactive.function.client.WebClientResponseException.Conflict;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrustedPartyWeb UnitTest")
class OrganizationWebImplTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private String TRUSTED_PARTY_URL = "http://localhost:8020/api/v1";
  private String ORGANIZATION_ID = "6fb292aa-ee38-48ae-998f-079ad9d01e7b";

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
  private String INT1_CERT = """
      -----BEGIN CERTIFICATE-----
      MIICAzCCAamgAwIBAgIUXasg/hr17lvGO6wzCe9psqHFD9MwCgYIKoZIzj0EAwIw
      bTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug
      RGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp
      Y2F0ZSBBdXRob3JpdHkwHhcNMjYwMzMxMTAyNDM1WhcNMzEwMzMwMTAyNDM1WjAu
      MQswCQYDVQQGEwJDWjEMMAoGA1UECgwDQ1BGMREwDwYDVQQDDAhjcGYtaW50MTBZ
      MBMGByqGSM49AgEGCCqGSM49AwEHA0IABLmZq7t7VPs6bJyNnH6NUBAxvSCEdRRS
      QGq+ESZc5+zETASkOPfqHEQkc/atqz+d7X/Sf/aDn7JwZ0mnEC6hCpqjZjBkMBIG
      A1UdEwEB/wQIMAYBAf8CAQEwDgYDVR0PAQH/BAQDAgEGMB0GA1UdDgQWBBRpLdHn
      PMpijgKLlLtiD+dzzS8GqTAfBgNVHSMEGDAWgBQshCsItY73T6n4lPjXR/574ez2
      GTAKBggqhkjOPQQDAgNIADBFAiAAo3t8i/BVbOZ68JKn/j+YTzRlFyohlj1nd6qc
      rokKFAIhAJK4xAJfPdQr89D1PzKh8GAJiGqbeJh0f4UY/bSIXcwl
      -----END CERTIFICATE-----
        """;
  private String INT2_CERT = """
      -----BEGIN CERTIFICATE-----
      MIIBxDCCAWqgAwIBAgIUTwRHh0gkw0wGo95BQ0erOuMtQ24wCgYIKoZIzj0EAwIw
      LjELMAkGA1UEBhMCQ1oxDDAKBgNVBAoMA0NQRjERMA8GA1UEAwwIY3BmLWludDEw
      HhcNMjYwMzMxMTAyNDQzWhcNMzEwMzMwMTAyNDQzWjAuMQswCQYDVQQGEwJDWjEM
      MAoGA1UECgwDQ1BGMREwDwYDVQQDDAhjcGYtaW50MjBZMBMGByqGSM49AgEGCCqG
      SM49AwEHA0IABA9JvLi5DwAZixixQ41Xnaqzg5HjAAl9X1JQot0wVvfG1cws3pX4
      3vW6fYfkmUON3MKlNmZxsUvkD6lD1+QqaF+jZjBkMBIGA1UdEwEB/wQIMAYBAf8C
      AQAwDgYDVR0PAQH/BAQDAgEGMB0GA1UdDgQWBBRa40NTdW5DcvncZAk3TAejmydY
      1TAfBgNVHSMEGDAWgBRpLdHnPMpijgKLlLtiD+dzzS8GqTAKBggqhkjOPQQDAgNI
      ADBFAiBXucydJVT/fLBX5UwZPN2eZATmGK04wBgeSY0xuGC5SAIhAJjmBRNQm1lD
      pxKlNY7frNF+N5eoC9UA6cOqvRtJ2C5r
      -----END CERTIFICATE-----
      """;

  @Mock
  private Organization organization;

  @Mock
  private TrustedParty trustedParty;

  @Mock
  private Client client;

  @Mock
  private WebClient webClient;

  private OrganizationWeb organizationWeb;

  @BeforeEach
  void setUp() {
    organizationWeb = new OrganizationWebImpl(client);

    when(client.trustedParty(Optional.empty())).thenReturn(webClient);
    when(client.getDefaultTrustedPartyUrl()).thenReturn(TRUSTED_PARTY_URL);

    when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
    when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));

    when(trustedParty.getUrlIfNotDefault()).thenReturn(Optional.empty());
  }

  @Nested
  @DisplayName("create")
  class Create {
    private String CREATE_URI = "/organizations/" + ORGANIZATION_ID;

    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Register new Organization")
      void create_pass() {
        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPostRequest(webClient, CREATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<RegisterOrganizationFormDTO, Mono<Void>>) form -> Mono.empty());

        StepVerifier.create(
            organizationWeb.create(organization),
            StepVerifierOptions.create().scenarioName("should register new Organization"))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Should handle Unauthorized")
      void create_handleUnauthorized() {
        Unauthorized unauthorized = ((Unauthorized) buildHttpException(CREATE_URI, HttpStatus.UNAUTHORIZED, "Unauthorized"));

        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPostRequest(webClient, CREATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<RegisterOrganizationFormDTO, Mono<Void>>) form -> Mono.error(() -> unauthorized));

        StepVerifier.create(
            organizationWeb.create(organization),
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
      void create_handleBadRequest() {
        BadRequest badRequest = ((BadRequest) buildHttpException(CREATE_URI, HttpStatus.BAD_REQUEST, "Bad Request"));

        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPostRequest(webClient, CREATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<RegisterOrganizationFormDTO, Mono<Void>>) form -> Mono.error(() -> badRequest));

        StepVerifier.create(
            organizationWeb.create(organization),
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
      @DisplayName("Should handle Conflict")
      void create_handleConflict() {
        Conflict conflict = ((Conflict) buildHttpException(CREATE_URI, HttpStatus.CONFLICT, "Conflict"));

        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPostRequest(webClient, CREATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<RegisterOrganizationFormDTO, Mono<Void>>) form -> Mono.error(() -> conflict));

        StepVerifier.create(
            organizationWeb.create(organization),
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
      void create_handleUnexpect() {
        Throwable unexpect = new Throwable("Some exception");

        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPostRequest(webClient, CREATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<RegisterOrganizationFormDTO, Mono<Void>>) form -> Mono.error(() -> unexpect));

        StepVerifier.create(
            organizationWeb.create(organization),
            StepVerifierOptions.create().scenarioName("should fail with exact InternalApplicationException if unexpected exception"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(InternalApplicationException.class, exception, "should be InternalApplicationException");
              assertEquals(
                  "New organization with identifier '6fb292aa-ee38-48ae-998f-079ad9d01e7b' has not been registered!",
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
  @DisplayName("updateOrganizationCertificate")
  class UpdateOrganizationCertificate {
    private String UPDATE_URI = "/organizations/" + ORGANIZATION_ID + "/certs";

    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Update existing Organization")
      void updateOrganizationCertificate_pass() {
        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPutRequest(webClient, UPDATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<UpdateOrganizationFormDTO, Mono<Void>>) form -> Mono.empty());

        StepVerifier.create(
            organizationWeb.updateOrganizationCertificate(organization),
            StepVerifierOptions.create().scenarioName("should update existing Organization"))
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Should handle BadRequest")
      void updateOrganizationCertificate_handleBadRequest() {
        BadRequest badRequest = ((BadRequest) buildHttpException(UPDATE_URI, HttpStatus.BAD_REQUEST, "Bad Request"));

        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPutRequest(webClient, UPDATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<UpdateOrganizationFormDTO, Mono<Void>>) form -> Mono.error(() -> badRequest));

        StepVerifier.create(
            organizationWeb.updateOrganizationCertificate(organization),
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
      void updateOrganizationCertificate_handleNotFound() {
        NotFound notFound = ((NotFound) buildHttpException(UPDATE_URI, HttpStatus.NOT_FOUND, "Not Found"));

        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPutRequest(webClient, UPDATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<UpdateOrganizationFormDTO, Mono<Void>>) form -> Mono.error(() -> notFound));

        StepVerifier.create(
            organizationWeb.updateOrganizationCertificate(organization),
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
      @DisplayName("Should handle Unauthorized")
      void updateOrganizationCertificate_handleUnauthorized() {
        Unauthorized unauthorized = ((Unauthorized) buildHttpException(UPDATE_URI, HttpStatus.UNAUTHORIZED, "Unauthorized"));

        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPutRequest(webClient, UPDATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<UpdateOrganizationFormDTO, Mono<Void>>) form -> Mono.error(() -> unauthorized));

        StepVerifier.create(
            organizationWeb.updateOrganizationCertificate(organization),
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
      @DisplayName("Should handle Conflict")
      void updateOrganizationCertificate_handleConflict() {
        Conflict conflict = ((Conflict) buildHttpException(UPDATE_URI, HttpStatus.CONFLICT, "Conflict"));

        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPutRequest(webClient, UPDATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<UpdateOrganizationFormDTO, Mono<Void>>) form -> Mono.error(() -> conflict));

        StepVerifier.create(
            organizationWeb.updateOrganizationCertificate(organization),
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
      void updateOrganizationCertificate_handleUnexpect() {
        Throwable unexpect = new Throwable("Some exception");

        when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
        when(organization.getIntermediateCertificates()).thenReturn(List.of(INT1_CERT, INT2_CERT));

        when(client.sendPutRequest(webClient, UPDATE_URI, Void.class, Map.of()))
            .thenAnswer(_ -> (Function<UpdateOrganizationFormDTO, Mono<Void>>) form -> Mono.error(() -> unexpect));

        StepVerifier.create(
            organizationWeb.updateOrganizationCertificate(organization),
            StepVerifierOptions.create().scenarioName("should fail with exact InternalApplicationException if unexpected exception"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(InternalApplicationException.class, exception, "should be InternalApplicationException");
              assertEquals(
                  "Certificates for organization with identifier '6fb292aa-ee38-48ae-998f-079ad9d01e7b' has not been updated!",
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
  @DisplayName("getById")
  class GetById {
    private String GET_URI = "/organizations/" + ORGANIZATION_ID;

    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Get Organization")
      void getById_pass() {
        OrganizationResponseDTO response = new OrganizationResponseDTO(ORGANIZATION_ID, ORGANIZATION_CERT);

        when(client.sendGetOneRequest(webClient, GET_URI, OrganizationResponseDTO.class, Map.of()))
            .thenReturn(Mono.just(response));

        StepVerifier.create(
            organizationWeb.getById(organization),
            StepVerifierOptions.create().scenarioName("should update existing Organization"))
            .assertNext((Organization orgModel) -> {
              System.out.println(orgModel);
              assertEquals(ORGANIZATION_ID, orgModel.getIdentifier(), "should have exact id");
              assertEquals(ORGANIZATION_CERT, orgModel.getClientCertificate(), "should have exact client certificate");
            })
            .verifyComplete();
      }
    }

    @Nested
    @DisplayName("ErrorPaty")
    class ErrorPaty {
      @Test
      @DisplayName("Should handle NotFound")
      void getById_handleNotFound() {
        when(client.sendGetOneRequest(webClient, GET_URI, OrganizationResponseDTO.class, Map.of()))
            .thenReturn(Mono.error(() -> ((NotFound) buildHttpException(GET_URI, HttpStatus.NOT_FOUND, "Not Found"))));

        StepVerifier.create(
            organizationWeb.getById(organization),
            StepVerifierOptions.create().scenarioName("should fail with exact NotFoundException if http request fail with NotFound"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(NotFoundException.class, exception, "should be NotFoundException");
              assertEquals(
                  "TrustedParty responded with status '404 NOT_FOUND' and message 'Not Found'!",
                  exception.getMessage(),
                  "should have exact error message");
            });
      }

      @Test
      @DisplayName("Should handle Unexpect exceptions as InternalApplicationException")
      void updateOrganizationCertificate_handleUnexpect() {

        when(client.sendGetOneRequest(webClient, GET_URI, OrganizationResponseDTO.class, Map.of()))
            .thenReturn(Mono.error(() -> new Throwable("Some exception")));

        StepVerifier.create(
            organizationWeb.getById(organization),
            StepVerifierOptions.create().scenarioName("should fail with exact InternalApplicationException if unexpected exception"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(ApplicationException.class, exception, "should be ApplicationException");
              assertInstanceOf(InternalApplicationException.class, exception, "should be InternalApplicationException");
              assertEquals(
                  "Organization with identifier '6fb292aa-ee38-48ae-998f-079ad9d01e7b' has not been fetched!",
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
