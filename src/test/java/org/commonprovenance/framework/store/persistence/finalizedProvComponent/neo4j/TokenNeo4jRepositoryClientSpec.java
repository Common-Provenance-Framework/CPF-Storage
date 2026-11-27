package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TokenRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.TokenNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client.TokenNeo4jRepositoryClient;
import org.commonprovenance.framework.store.web.trustedParty.impl.TrustedPartyWebImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.vavr.control.Either;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenRepository Spec")
class TokenNeo4jRepositoryClientSpec {
  private final String DOCUMENT_IDENTIFIER = "a45e5ccb-c486-4f77-8655-b4de9a2bac00";
  private final String TOKEN_ID = "4:c07ee6e7-57c3-477f-9201-b05fab4c0316:71";
  private final String TRUSTED_PARTY_ID = "TrustedParty";

  @Mock
  private TokenNeo4jRepositoryClient client;

  @Mock
  private Token token;

  @Mock
  private TokenNode tokenNode;

  @Mock
  private TrustedParty trustedParty;

  @Mock
  private Document document;

  private TokenRepository repository;
  private ListAppender<ILoggingEvent> logAppender;
  private Logger storeWebLogger;

  @BeforeEach
  void setUp() {
    repository = new TokenNeo4jRepository(client);
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
  @DisplayName("save")
  class Save {
    @Test
    @DisplayName("Save new Token into neo4j database")
    void save_buildsNodeAndCallSave() {
      try (MockedStatic<TokenNodeFactory> tokenNodeFactory = mockStatic(TokenNodeFactory.class)) {

        tokenNodeFactory.when(() -> TokenNodeFactory.build(any(Token.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              Token t = invocation.getArgument(0);
              assertEquals(token, t, "Factory should receive exact token");

              return tokenNode;
            });

        when(client.save(any(TokenNode.class))).thenAnswer((InvocationOnMock invocation) -> {
          TokenNode node = invocation.getArgument(0);
          assertEquals(tokenNode, node, "Neo4j cient should receive exact token node");
          return Mono.just(node);
        });

        repository.save(token)
            .doOnError(_ -> logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(System.out::println))
            .block();

        tokenNodeFactory.verify(() -> TokenNodeFactory.build(token));
        verify(client).save(tokenNode);
      }
    }
  }

  @Nested
  @DisplayName("getTokenByDocumentIdentifier")
  class GetTokenByDocumentIdentifier {
    @Test
    @DisplayName("Find TokenNode by document identifier")
    void getTokenByDocumentIdentifier_findNodeAndBuildToken() {
      try (MockedStatic<TokenFactory> tokenFactory = mockStatic(TokenFactory.class)) {

        when(client.findTokenIdsByDocumentIdentifier(anyString())).thenAnswer((InvocationOnMock invocation) -> {
          String identifier = invocation.getArgument(0);
          assertEquals(DOCUMENT_IDENTIFIER, identifier, "Neo4j cient method 'findTokenIdsByDocumentIdentifier' should receive exact document identifier");
          return Flux.just(TOKEN_ID);
        });

        when(client.findById(anyString())).thenAnswer((InvocationOnMock invocation) -> {
          String id = invocation.getArgument(0);
          assertEquals(TOKEN_ID, id, "Neo4j cient method 'findById' should receive exact token id");
          return Mono.just(tokenNode);
        });

        tokenFactory.when(() -> TokenFactory.buildWithRelations(any(TokenNode.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              TokenNode node = invocation.getArgument(0);
              assertEquals(tokenNode, node, "Factory should receive exact token node");

              return Either.right(token);
            });

        repository.getTokenByDocumentIdentifier(DOCUMENT_IDENTIFIER)
            .doOnError(_ -> logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(System.out::println))
            .block();

        verify(client).findTokenIdsByDocumentIdentifier(DOCUMENT_IDENTIFIER);
        verify(client).findById(TOKEN_ID);
        tokenFactory.verify(() -> TokenFactory.buildWithRelations(tokenNode));
      }
    }
  }

  @Nested
  @DisplayName("getTokenIdByDocumentIdentifier")
  class GetTokenIdByDocumentIdentifier {
    @Test
    @DisplayName("Find TokenNode id by document identifier")
    void getTokenIdByDocumentIdentifier_findNodeId() {
      when(client.findTokenIdsByDocumentIdentifier(anyString())).thenAnswer((InvocationOnMock invocation) -> {
        String identifier = invocation.getArgument(0);
        assertEquals(DOCUMENT_IDENTIFIER, identifier, "Neo4j cient method 'findTokenIdsByDocumentIdentifier' should receive exact document identifier");
        return Flux.just(TOKEN_ID);
      });

      repository.getTokenIdByDocumentIdentifier(DOCUMENT_IDENTIFIER)
          .doOnError(_ -> logAppender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .forEach(System.out::println))
          .block();

      verify(client).findTokenIdsByDocumentIdentifier(DOCUMENT_IDENTIFIER);
    }
  }

  @Nested
  @DisplayName("connectWasIssuedBy")
  class ConnectWasIssuedBy {
    @Test
    @DisplayName("Create WasIssuedBy relation from Token to TrustedParty")
    void connectWasIssuedBy_createWasIssuedByRelationship() {
      when(document.getIdentifier()).thenReturn(Either.right(DOCUMENT_IDENTIFIER));
      when(trustedParty.getId()).thenReturn(TRUSTED_PARTY_ID);

      when(client.findTokenIdsByDocumentIdentifier(anyString())).thenAnswer((InvocationOnMock invocation) -> {
        String identifier = invocation.getArgument(0);
        assertEquals(DOCUMENT_IDENTIFIER, identifier, "Neo4j cient method 'findTokenIdsByDocumentIdentifier' should receive exact document identifier");
        return Flux.just(TOKEN_ID);
      });

      when(client.createWasIssuedByRelationship(anyString(), anyString())).thenAnswer((InvocationOnMock invocation) -> {
        String tokenId = invocation.getArgument(0);
        String tpId = invocation.getArgument(1);

        assertEquals(TOKEN_ID, tokenId, "Neo4j cient should receive exact TokenNode id");
        assertEquals(TRUSTED_PARTY_ID, tpId, "Neo4j cient should receive exact TrustedParty id (name)");

        return Mono.just(true);
      });

      repository.connectWasIssuedBy(Optional.of(trustedParty))
          .apply(document)
          .doOnError(_ -> logAppender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .forEach(System.out::println))
          .block();

      verify(client).findTokenIdsByDocumentIdentifier(DOCUMENT_IDENTIFIER);
      verify(client).createWasIssuedByRelationship(TOKEN_ID, TRUSTED_PARTY_ID);
    }
  }
}
