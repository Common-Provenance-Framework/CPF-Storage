package org.commonprovenance.framework.store.web.trustedParty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.MetaDocument;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.model.factory.TrustedPartyFactory;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.IssueTokenFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.VerifySignatureFormDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.IssueTokenFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.factory.VerifySignatureFormFactory;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TrustedPartyResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.impl.TrustedPartyWebImpl;
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
@DisplayName("TrustedPartyWeb Spec")
class TrustedPartyWebSpec {

  private static final String DEFAULT_TP_URL = "http://localhost:8020/api/v1";
  private static final String TP_URL = "http://localhost:80/api/v1";
  private static final String SIGNATURE = "...";

  @Mock
  private Client client;

  @Mock
  private WebClient defaultTpWebClient;

  @Mock
  private WebClient customTpWebClient;

  @Mock
  private Organization organization;

  @Mock
  private TrustedParty trustedParty;

  @Mock
  private Token token;

  @Mock
  private MetaDocument metaDocument;

  @Mock
  private IssueTokenFormDTO issueTokenFormDTO;

  @Mock
  private VerifySignatureFormDTO verifySignatureFormDTO;

  @Mock
  private TrustedPartyResponseDTO trustedPartyResponseDTO;

  @Mock
  private TokenResponseDTO tokenResponseDTO;

  private TrustedPartyWeb trustedPartyWeb;
  private ListAppender<ILoggingEvent> logAppender;
  private Logger storeWebLogger;

  @BeforeEach
  void setUp() {
    trustedPartyWeb = new TrustedPartyWebImpl(client);
    when(client.getDefaultTrustedPartyUrl()).thenReturn(DEFAULT_TP_URL);
  }

  @BeforeEach
  void setUpLogCapture() {
    storeWebLogger = (Logger) LoggerFactory.getLogger(TrustedPartyWebImpl.class);
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
  @DisplayName("getTrustedParty")
  class GetTrustedParty {
    @Test
    @DisplayName("Get default trusted party info")
    void getTrustedParty_default() {
      assertGetTrustedParty(true);
    }

    @Test
    @DisplayName("Get custom trusted party info")
    void getTrustedParty_custom() {
      assertGetTrustedParty(false);
    }

    private void assertGetTrustedParty(Boolean isDefault) {
      WebClient webClient = isDefault ? defaultTpWebClient : customTpWebClient;
      Optional<String> maybeUrl = isDefault ? Optional.empty() : Optional.of(TP_URL);
      String expectedUrl = maybeUrl.orElse(DEFAULT_TP_URL);

      try (MockedStatic<TrustedPartyFactory> trustedPartyFactory = mockStatic(TrustedPartyFactory.class)) {

        Function<TrustedPartyResponseDTO, Either<ApplicationException, TrustedParty>> factory = dto -> {
          assertEquals(trustedPartyResponseDTO, dto, "Factory should receive the expected DTO");
          return Either.right(trustedParty);
        };
        trustedPartyFactory.when(() -> TrustedPartyFactory.buildUnsafe(anyString(), anyBoolean()))
            .thenReturn(factory);

        when(client.trustedParty(any())).thenReturn(webClient);
        when(client.sendGetOneRequest(webClient, "/info", TrustedPartyResponseDTO.class, Map.of()))
            .thenReturn(Mono.just(trustedPartyResponseDTO));

        trustedPartyWeb.getTrustedParty(maybeUrl)
            .block();

        verify(client).trustedParty(maybeUrl);
        verify(client).sendGetOneRequest(webClient, "/info", TrustedPartyResponseDTO.class, Map.of());
        trustedPartyFactory.verify(() -> TrustedPartyFactory.buildUnsafe(expectedUrl, isDefault));
      }
    }

  }

  @Nested
  @DisplayName("issueGraphToken - Graph")
  class IssueGraphTokenGraph {
    @Test
    @DisplayName("Call default trusted party")
    void issueGraphToken_default() {
      assertIssueGraphToken(true);
    }

    @Test
    @DisplayName("Call custom trusted party")
    void issueGraphToken_custom() {
      assertIssueGraphToken(false);
    }

    private void assertIssueGraphToken(Boolean isDefault) {
      try (MockedStatic<IssueTokenFormFactory> issueTokenFormFactory = mockStatic(IssueTokenFormFactory.class);
          MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {

        WebClient webClient = isDefault ? defaultTpWebClient : customTpWebClient;
        Optional<String> maybeUrl = isDefault ? Optional.empty() : Optional.of(TP_URL);

        issueTokenFormFactory.when(() -> IssueTokenFormFactory.buildSafe(any(Organization.class), anyString()))
            .thenReturn(Either.right(issueTokenFormDTO));

        tokenFactory.when(() -> TokenFactory.build(any(TokenResponseDTO.class)))
            .thenReturn(Either.right(token));

        when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));
        when(trustedParty.getUrlIfNotDefault()).thenReturn(maybeUrl);

        when(client.trustedParty(any())).thenAnswer(invocation -> {
          return ((Optional<String>) invocation.getArgument(0)).isEmpty() ? defaultTpWebClient : customTpWebClient;
        });

        when(client.sendPostRequest(webClient, "/issueToken", TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> {
              assertEquals(issueTokenFormDTO, form, "Request should contain the built token form");
              return Mono.just(tokenResponseDTO);
            });

        trustedPartyWeb.issueGraphToken(SIGNATURE)
            .apply(organization)
            .block();

        issueTokenFormFactory.verify(() -> IssueTokenFormFactory.buildSafe(organization, SIGNATURE));
        verify(client).trustedParty(maybeUrl);
        verify(client).sendPostRequest(webClient, "/issueToken", TokenResponseDTO.class, Map.of());
        tokenFactory.verify(() -> TokenFactory.build(tokenResponseDTO));
      }
    }
  }

  @Nested
  @DisplayName("issueGraphToken - GraphType")
  class IssueGraphTokenByGraphType {
    @Test
    @DisplayName("Call default trusted party")
    void issueGraphToken_graphType_default() {
      assertIssueGraphTokenByGraphType(true);
    }

    @Test
    @DisplayName("Call custom trusted party")
    void issueGraphToken_graphType_custom() {
      assertIssueGraphTokenByGraphType(false);
    }

    private void assertIssueGraphTokenByGraphType(Boolean isDefault) {
      try (MockedStatic<IssueTokenFormFactory> issueTokenFormFactory = mockStatic(IssueTokenFormFactory.class);
          MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {

        WebClient webClient = isDefault ? defaultTpWebClient : customTpWebClient;
        Optional<String> maybeUrl = isDefault ? Optional.empty() : Optional.of(TP_URL);

        issueTokenFormFactory.when(() -> IssueTokenFormFactory.buildSafe(any(Organization.class), any(GraphType.class)))
            .thenReturn(Either.right(issueTokenFormDTO));

        tokenFactory.when(() -> TokenFactory.build(any(TokenResponseDTO.class)))
            .thenReturn(Either.right(token));

        when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));
        when(trustedParty.getUrlIfNotDefault()).thenReturn(maybeUrl);

        when(client.trustedParty(any())).thenAnswer(invocation -> {
          return ((Optional<String>) invocation.getArgument(0)).isEmpty() ? defaultTpWebClient : customTpWebClient;
        });

        when(client.sendPostRequest(webClient, "/issueToken", TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> {
              assertEquals(issueTokenFormDTO, form, "Request should contain the built token form");
              return Mono.just(tokenResponseDTO);
            });

        trustedPartyWeb.issueGraphToken(GraphType.BACKBONE)
            .apply(organization)
            .block();

        issueTokenFormFactory.verify(() -> IssueTokenFormFactory.buildSafe(organization, GraphType.BACKBONE));
        verify(client).trustedParty(maybeUrl);
        verify(client).sendPostRequest(webClient, "/issueToken", TokenResponseDTO.class, Map.of());
        tokenFactory.verify(() -> TokenFactory.build(tokenResponseDTO));
      }

    }
  }

  @Nested
  @DisplayName("issueGraphToken - MetaDocument")
  class IssueMetaDocumentToken {
    @Test
    @DisplayName("Call default trusted party")
    void issueGraphToken_metaDocument_default() {
      assertIssueGraphTokenMetaDocument(true);
    }

    @Test
    @DisplayName("Call custom trusted party")
    void issueGraphToken_metaDocument_custom() {
      when(trustedParty.getUrl()).thenReturn(Optional.of(TP_URL));

      assertIssueGraphTokenMetaDocument(false);
    }

    private void assertIssueGraphTokenMetaDocument(Boolean isDefault) {
      try (MockedStatic<IssueTokenFormFactory> issueTokenFormFactory = mockStatic(IssueTokenFormFactory.class);
          MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {

        WebClient webClient = isDefault ? defaultTpWebClient : customTpWebClient;
        Optional<String> maybeUrl = isDefault ? Optional.empty() : Optional.of(TP_URL);

        issueTokenFormFactory.when(() -> IssueTokenFormFactory.buildSafe(any(MetaDocument.class)))
            .thenReturn(Either.right(issueTokenFormDTO));

        tokenFactory.when(() -> TokenFactory.build(any(TokenResponseDTO.class)))
            .thenReturn(Either.right(token));

        when(metaDocument.getTrustedParty(anyString())).thenReturn(trustedParty);
        when(trustedParty.getIsDefault()).thenReturn(isDefault);

        when(client.trustedParty(any())).thenAnswer(invocation -> {
          return ((Optional<String>) invocation.getArgument(0)).isEmpty() ? defaultTpWebClient : customTpWebClient;
        });

        when(client.sendPostRequest(webClient, "/issueToken", TokenResponseDTO.class, Map.of()))
            .thenAnswer(_ -> (Function<IssueTokenFormDTO, Mono<TokenResponseDTO>>) form -> {
              assertEquals(issueTokenFormDTO, form, "Request should contain the built token form");
              return Mono.just(tokenResponseDTO);
            });

        trustedPartyWeb.issueGraphToken(metaDocument)
            .block();

        issueTokenFormFactory.verify(() -> IssueTokenFormFactory.buildSafe(metaDocument));
        verify(metaDocument).getTrustedParty(DEFAULT_TP_URL);
        verify(client).trustedParty(maybeUrl);
        verify(client).sendPostRequest(webClient, "/issueToken", TokenResponseDTO.class, Map.of());
        tokenFactory.verify(() -> TokenFactory.build(tokenResponseDTO));
      }

    }
  }

  @Nested
  @DisplayName("verifySignature")
  class VerifySignature {
    @Test
    @DisplayName("Call default trusted party")
    void verifySignature_default() {
      assertVerifySignature(true);
    }

    @Test
    @DisplayName("Call custom trusted party")
    void verifySignature_custom() {
      assertVerifySignature(false);
    }

    private void assertVerifySignature(Boolean isDefault) {
      try (MockedStatic<VerifySignatureFormFactory> formFactory = mockStatic(VerifySignatureFormFactory.class)) {

        WebClient webClient = isDefault ? defaultTpWebClient : customTpWebClient;
        Optional<String> maybeUrl = isDefault ? Optional.empty() : Optional.of(TP_URL);

        Function<Organization, Either<ApplicationException, VerifySignatureFormDTO>> factory = value -> {
          assertEquals(organization, value, "Factory should receive the organization being verified");
          return Either.right(verifySignatureFormDTO);
        };
        formFactory.when(() -> VerifySignatureFormFactory.buildSafe(anyString())).thenReturn(factory);

        when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));
        when(trustedParty.getUrlIfNotDefault()).thenReturn(maybeUrl);

        when(client.trustedParty(any())).thenAnswer(invocation -> {
          return ((Optional<String>) invocation.getArgument(0)).isEmpty() ? defaultTpWebClient : customTpWebClient;
        });
        when(client.sendPostRequest(webClient, "/verifySignature", Boolean.class, Map.of()))
            .thenAnswer(_ -> (Function<VerifySignatureFormDTO, Mono<Boolean>>) form -> {
              assertEquals(verifySignatureFormDTO, form, "Request should contain the built verification form");
              return Mono.just(true);
            });

        trustedPartyWeb.verifySignature(SIGNATURE)
            .apply(organization)
            .block();

        formFactory.verify(() -> VerifySignatureFormFactory.buildSafe(SIGNATURE));
        verify(client).trustedParty(maybeUrl);
        verify(client).sendPostRequest(webClient, "/verifySignature", Boolean.class, Map.of());
      }
    }
  }

}
