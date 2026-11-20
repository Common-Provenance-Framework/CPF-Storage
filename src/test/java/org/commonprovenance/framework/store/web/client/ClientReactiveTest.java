package org.commonprovenance.framework.store.web.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.commonprovenance.framework.store.web.client.reactive.ClientReactive;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.ErrorResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.OrganizationResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientReactive Spec")
public class ClientReactiveTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private String URL = "http://example.test";
  @Mock
  private WebConfig config;

  private String ORGANIZATION_ID = "6fb292aa-ee38-48ae-998f-079ad9d01e7b";

  private String ORGANIZATION_CERT = """
      -----BEGIN CERTIFICATE-----
      org_cert_1
      -----END CERTIFICATE-----
      """;

  private String ORGANIZATION_ID_2 = "6fb292aa-ee38-48ae-998f-079ad9d01e7b";

  private String ORGANIZATION_CERT_2 = """
      -----BEGIN CERTIFICATE-----
      org_cert_2
      -----END CERTIFICATE-----
      """;

  private OrganizationResponseDTO DTO = new OrganizationResponseDTO(ORGANIZATION_ID, ORGANIZATION_CERT);
  private OrganizationResponseDTO DTO_2 = new OrganizationResponseDTO(ORGANIZATION_ID_2, ORGANIZATION_CERT_2);

  // private ClientReactive client;
  private ListAppender<ILoggingEvent> logAppender;
  private Logger clientLogger;
  private Level originalLogLevel;

  @BeforeEach
  void setUpLogCapture() {
    clientLogger = (Logger) LoggerFactory.getLogger(ClientReactive.class);
    originalLogLevel = clientLogger.getLevel();
    clientLogger.setLevel(Level.TRACE);
    logAppender = new ListAppender<>();
    logAppender.start();
    clientLogger.addAppender(logAppender);
  }

  @AfterEach
  void tearDownLogCapture() {
    clientLogger.detachAppender(logAppender);
    clientLogger.setLevel(originalLogLevel);
  }

  @Nested
  @DisplayName("HEAD")
  class Head {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("HEAD request completes without a response body")
      void head_success() {
        ClientReactive client = clientWithResponse(HttpStatus.OK, Optional.empty());

        StepVerifier.create(
            client.sendHeadRequest(
                client.store(),
                "/resource",
                Map.of()),
            StepVerifierOptions.create().scenarioName("HEAD request should complete"))
            .verifyComplete();

        assertTrue(logAppender.list.isEmpty(), "HEAD has no value to log at TRACE");
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Logs Mono errors and WebClient response bodies")
      void head_error() {
        ErrorResponseDTO dto = buildErrorResponse("/resource", HttpStatus.BAD_REQUEST, "Bad Request");
        ClientReactive client = clientWithResponse(HttpStatus.BAD_REQUEST, Optional.of(dto));

        StepVerifier.create(
            client.sendHeadRequest(
                client.store(),
                "/resource",
                Map.of()),
            StepVerifierOptions.create().scenarioName("HEAD request should fail with status 400"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(WebClientResponseException.class, exception, "should be WebClientResponseException");
              assertInstanceOf(BadRequest.class, exception, "should be BadRequest");
              assertEquals("400 Bad Request from UNKNOWN ", exception.getMessage(), "should have exact error message");

              assertEquals(2, logAppender.list.size(), "should log each result");
              assertEquals(Level.ERROR, logAppender.list.get(0).getLevel());
              assertEquals("Client: 400 Bad Request from UNKNOWN ", logAppender.list.get(0).getFormattedMessage());
              assertEquals(Level.DEBUG, logAppender.list.get(1).getLevel());
              assertEquals("Client: " + ((WebClientResponseException) exception).getResponseBodyAsString(), logAppender.list.get(1).getFormattedMessage());
            });

      }
    }
  }

  @Nested
  @DisplayName("GET")
  class Get {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("Get Mono value and log TRACE")
      void get_successMono() {
        ClientReactive client = clientWithResponse(HttpStatus.OK, Optional.of(DTO));

        StepVerifier.create(
            client.sendGetOneRequest(
                client.store(),
                "/resource",
                OrganizationResponseDTO.class,
                Map.of()),
            StepVerifierOptions.create().scenarioName("should get DTO"))
            .assertNext((OrganizationResponseDTO response) -> {
              assertEquals(DTO, response);

            })
            .verifyComplete();

        assertEquals(1, logAppender.list.size(), "should log result");
        assertEquals(Level.TRACE, logAppender.list.get(0).getLevel());
        assertEquals("Client: " + DTO, logAppender.list.get(0).getFormattedMessage());
      }

      @Test
      @DisplayName("Get Flux values and log TRACE")
      void get_successFlux() {
        ClientReactive client = clientWithResponse(HttpStatus.OK, Optional.of(List.of(DTO, DTO_2)));

        StepVerifier.create(
            client.sendGetManyRequest(
                client.store(),
                "/resource",
                OrganizationResponseDTO.class,
                Map.of()),
            StepVerifierOptions.create().scenarioName("should get DTOs"))
            .expectNext(DTO, DTO_2)
            .verifyComplete();

        assertEquals(2, logAppender.list.size(), "should log each result");
        assertEquals(Level.TRACE, logAppender.list.get(0).getLevel());
        assertEquals("Client: " + DTO, logAppender.list.get(0).getFormattedMessage());
        assertEquals(Level.TRACE, logAppender.list.get(1).getLevel());
        assertEquals("Client: " + DTO_2, logAppender.list.get(1).getFormattedMessage());
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {

      @Test
      @DisplayName("Logs Mono errors and WebClient response bodies")
      void get_error() {

        ErrorResponseDTO dto = buildErrorResponse("/resource", HttpStatus.BAD_REQUEST, "Bad Request");
        ClientReactive client = clientWithResponse(HttpStatus.BAD_REQUEST, Optional.of(dto));

        StepVerifier.create(
            client.sendGetOneRequest(client.store(), "/resource", OrganizationResponseDTO.class, Map.of()),
            StepVerifierOptions.create().scenarioName("GET request should fail with status 400"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(WebClientResponseException.class, exception, "should be WebClientResponseException");
              assertInstanceOf(BadRequest.class, exception, "should be BadRequest");
              assertEquals("400 Bad Request from UNKNOWN ", exception.getMessage(), "should have exact error message");

              assertEquals(2, logAppender.list.size(), "should log each result");
              assertEquals(Level.ERROR, logAppender.list.get(0).getLevel());
              assertEquals("Client: 400 Bad Request from UNKNOWN ", logAppender.list.get(0).getFormattedMessage());
              assertEquals(Level.DEBUG, logAppender.list.get(1).getLevel());
              assertEquals("Client: " + ((WebClientResponseException) exception).getResponseBodyAsString(), logAppender.list.get(1).getFormattedMessage());
            });
      }
    }

  }

  @Nested
  @DisplayName("POST")
  class Post {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("POST request completes without a response body")
      void post_success() {
        ClientReactive client = clientWithResponse(HttpStatus.OK, Optional.empty());

        StepVerifier.create(
            client.sendPostRequest(
                client.store(),
                "/resource",
                Void.class,
                Map.of())
                .apply(DTO),
            StepVerifierOptions.create().scenarioName("POST request should complete"))
            .verifyComplete();

        assertTrue(logAppender.list.isEmpty(), "POST has no value to log at TRACE");
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Logs Mono errors and WebClient response bodies")
      void post_error() {
        ErrorResponseDTO dto = buildErrorResponse("/resource", HttpStatus.BAD_REQUEST, "Bad Request");
        ClientReactive client = clientWithResponse(HttpStatus.BAD_REQUEST, Optional.of(dto));

        StepVerifier.create(
            client.sendPostRequest(
                client.store(),
                "/resource",
                Void.class,
                Map.of())
                .apply(DTO),
            StepVerifierOptions.create().scenarioName("POST request should fail with status 400"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(WebClientResponseException.class, exception, "should be WebClientResponseException");
              assertInstanceOf(BadRequest.class, exception, "should be BadRequest");
              assertEquals("400 Bad Request from UNKNOWN ", exception.getMessage(), "should have exact error message");

              assertEquals(2, logAppender.list.size(), "should log each result");
              assertEquals(Level.ERROR, logAppender.list.get(0).getLevel());
              assertEquals("Client: 400 Bad Request from UNKNOWN ", logAppender.list.get(0).getFormattedMessage());
              assertEquals(Level.DEBUG, logAppender.list.get(1).getLevel());
              assertEquals("Client: " + ((WebClientResponseException) exception).getResponseBodyAsString(), logAppender.list.get(1).getFormattedMessage());
            });
      }
    }
  }

  @Nested
  @DisplayName("PUT")
  class Put {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("PUT request completes without a response body")
      void put_success() {
        ClientReactive client = clientWithResponse(HttpStatus.OK, Optional.empty());

        StepVerifier.create(
            client.sendPutRequest(
                client.store(),
                "/resource",
                Void.class,
                Map.of())
                .apply(DTO),
            StepVerifierOptions.create().scenarioName("PUT request should compliet"))
            .verifyComplete();

        assertTrue(logAppender.list.isEmpty(), "PUT has no value to log at TRACE");
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Logs Mono errors and WebClient response bodies")
      void put_error() {
        ErrorResponseDTO dto = buildErrorResponse("/resource", HttpStatus.BAD_REQUEST, "Bad Request");
        ClientReactive client = clientWithResponse(HttpStatus.BAD_REQUEST, Optional.of(dto));

        StepVerifier.create(
            client.sendPutRequest(
                client.store(),
                "/resource",
                Void.class,
                Map.of())
                .apply(DTO),
            StepVerifierOptions.create().scenarioName("PUT request should fail with status 400"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(WebClientResponseException.class, exception, "should be WebClientResponseException");
              assertInstanceOf(BadRequest.class, exception, "should be BadRequest");
              assertEquals("400 Bad Request from UNKNOWN ", exception.getMessage(), "should have exact error message");

              assertEquals(2, logAppender.list.size(), "should log each result");
              assertEquals(Level.ERROR, logAppender.list.get(0).getLevel());
              assertEquals("Client: 400 Bad Request from UNKNOWN ", logAppender.list.get(0).getFormattedMessage());
              assertEquals(Level.DEBUG, logAppender.list.get(1).getLevel());
              assertEquals("Client: " + ((WebClientResponseException) exception).getResponseBodyAsString(), logAppender.list.get(1).getFormattedMessage());
            });
      }
    }
  }

  @Nested
  @DisplayName("DELETE")
  class Delete {
    @Nested
    @DisplayName("HappyPath")
    class HappyPath {
      @Test
      @DisplayName("DELETE request completes without a response body")
      void delete_success() {
        ClientReactive client = clientWithResponse(HttpStatus.OK, Optional.empty());

        StepVerifier.create(
            client.sendDeleteRequest(
                client.store(),
                "/resource",
                Void.class,
                Map.of()),
            StepVerifierOptions.create().scenarioName("DELETE request should compliet"))
            .verifyComplete();

        assertTrue(logAppender.list.isEmpty(), "DELETE has no value to log at TRACE");
      }
    }

    @Nested
    @DisplayName("ErrorPath")
    class ErrorPath {
      @Test
      @DisplayName("Logs Mono errors and WebClient response bodies")
      void delete_error() {
        ErrorResponseDTO dto = buildErrorResponse("/resource", HttpStatus.BAD_REQUEST, "Bad Request");
        ClientReactive client = clientWithResponse(HttpStatus.BAD_REQUEST, Optional.of(dto));

        StepVerifier.create(
            client.sendDeleteRequest(
                client.store(),
                "/resource",
                Void.class,
                Map.of()),
            StepVerifierOptions.create().scenarioName("DELETE request should fail with status 400"))
            .verifyErrorSatisfies(exception -> {
              assertInstanceOf(WebClientResponseException.class, exception, "should be WebClientResponseException");
              assertInstanceOf(BadRequest.class, exception, "should be BadRequest");
              assertEquals("400 Bad Request from UNKNOWN ", exception.getMessage(), "should have exact error message");

              assertEquals(2, logAppender.list.size(), "should log each result");
              assertEquals(Level.ERROR, logAppender.list.get(0).getLevel());
              assertEquals("Client: 400 Bad Request from UNKNOWN ", logAppender.list.get(0).getFormattedMessage());
              assertEquals(Level.DEBUG, logAppender.list.get(1).getLevel());
              assertEquals("Client: " + ((WebClientResponseException) exception).getResponseBodyAsString(), logAppender.list.get(1).getFormattedMessage());
            });
      }
    }
  }

  private <T> ClientReactive clientWithResponse(HttpStatus status, Optional<T> maybeData) {
    WebClient webClient = WebClient.builder()
        .baseUrl(URL)
        .exchangeFunction(request -> {
          ClientResponse.Builder response = ClientResponse.create(status);
          maybeData.ifPresentOrElse(
              data -> response
                  .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                  .body(objectMapper.writeValueAsString(data)),
              () -> response
                  .body(""));
          return Mono.just(response.build());
        })
        .build();
    return new ClientReactive(webClient, webClient, webClient, config);
  }

  private ErrorResponseDTO buildErrorResponse(String requestUri, HttpStatus status, String message) {
    return new ErrorResponseDTO(LocalDateTime.of(2026, 11, 16, 0, 0),
        status,
        message,
        "/api/v1" + requestUri);
  }
}
