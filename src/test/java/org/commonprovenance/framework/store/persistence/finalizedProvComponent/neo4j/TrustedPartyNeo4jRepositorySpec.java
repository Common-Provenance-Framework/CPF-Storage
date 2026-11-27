package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.TrustedPartyFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.TrustedPartyRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.TrustedPartyNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client.TrustedPartyNeo4jRepositoryClient;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrustedPartyRepository Spec")
class TrustedPartyNeo4jRepositorySpec {
  private final String ORGANIZATION_IDENTIFIER = "a45e5ccb-c486-4f77-8655-b4de9a2bac00";
  private final String TRUSTED_PARTY_ID = "4:c07ee6e7-57c3-477f-9201-b05fab4c0316:71";
  private final String TRUSTED_PARTY_IDENTIFIER = "TrustedParty";
  private final String TRUSTED_PARTY_URL = "http://localhost:8020/api/v1/";

  @Mock
  private TrustedPartyNeo4jRepositoryClient client;

  @Mock
  private TrustedParty trustedParty;

  @Mock
  private TrustedPartyNode trustedPartyNode;

  private TrustedPartyRepository repository;
  private ListAppender<ILoggingEvent> logAppender;
  private Logger storeWebLogger;

  @BeforeEach
  void setUp() {
    repository = new TrustedPartyNeo4jRepository(client);
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
    @DisplayName("Save new TrustedParty into neo4j database")
    void save_buildsNodeAndCallSave() {
      try (MockedStatic<TrustedPartyNodeFactory> trustedPartyNodeFactory = mockStatic(TrustedPartyNodeFactory.class)) {

        trustedPartyNodeFactory.when(() -> TrustedPartyNodeFactory.build(any(TrustedParty.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              TrustedParty tp = invocation.getArgument(0);
              assertEquals(trustedParty, tp, "Factory should receive exact trustedParty");

              return trustedPartyNode;
            });

        when(client.save(any(TrustedPartyNode.class))).thenAnswer((InvocationOnMock invocation) -> {
          TrustedPartyNode node = invocation.getArgument(0);
          assertEquals(trustedPartyNode, node, "Neo4j cient should receive exact TrustedParty node");
          return Mono.just(node);
        });

        repository.save(trustedParty)
            .doOnError(_ -> logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(System.out::println))
            .block();

        trustedPartyNodeFactory.verify(() -> TrustedPartyNodeFactory.build(trustedParty));
        verify(client).save(trustedPartyNode);
      }
    }
  }

  @Nested
  @DisplayName("findByName")
  class FindByName {
    @Test
    @DisplayName("Find TrustedParty by its identifier (name)")
    void findByName_findIdThenNodeAndBuildModel() {
      try (MockedStatic<TrustedPartyFactory> trustedPartyFactory = mockStatic(TrustedPartyFactory.class)) {

        when(client.findIdByName(anyString())).thenAnswer((InvocationOnMock invocation) -> {
          String name = invocation.getArgument(0);
          assertEquals(TRUSTED_PARTY_IDENTIFIER, name, "Neo4j cient method 'findIdByName' should receive exact TrustedParty identifier (name)");
          return Flux.just(TRUSTED_PARTY_ID);
        });

        when(client.findById(anyString())).thenAnswer((InvocationOnMock invocation) -> {
          String id = invocation.getArgument(0);
          assertEquals(TRUSTED_PARTY_ID, id, "Neo4j cient method 'findById' should receive exact TrustedParty node id");
          return Mono.just(trustedPartyNode);
        });

        trustedPartyFactory.when(() -> TrustedPartyFactory.build(any(TrustedPartyNode.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              TrustedPartyNode node = invocation.getArgument(0);
              assertEquals(trustedPartyNode, node, "Factory should receive exact TrustedParty node");

              return trustedParty;
            });

        repository.findByName(TRUSTED_PARTY_IDENTIFIER)
            .doOnError(_ -> logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(System.out::println))
            .block();

        verify(client).findIdByName(TRUSTED_PARTY_IDENTIFIER);
        verify(client).findById(TRUSTED_PARTY_ID);
        trustedPartyFactory.verify(() -> TrustedPartyFactory.build(trustedPartyNode));
      }
    }
  }

  @Nested
  @DisplayName("findDefault")
  class FindDefault {
    @Test
    @DisplayName("Find default TrustedParty")
    void findDefault_findIdThenNodeAndBuildModel() {
      try (MockedStatic<TrustedPartyFactory> trustedPartyFactory = mockStatic(TrustedPartyFactory.class)) {

        when(client.findDefaultId()).thenAnswer((InvocationOnMock _) -> {
          return Flux.just(TRUSTED_PARTY_ID);
        });

        when(client.findById(anyString())).thenAnswer((InvocationOnMock invocation) -> {
          String id = invocation.getArgument(0);
          assertEquals(TRUSTED_PARTY_ID, id, "Neo4j cient method 'findById' should receive exact TrustedParty node id");
          return Mono.just(trustedPartyNode);
        });

        trustedPartyFactory.when(() -> TrustedPartyFactory.build(any(TrustedPartyNode.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              TrustedPartyNode node = invocation.getArgument(0);
              assertEquals(trustedPartyNode, node, "Factory should receive exact TrustedParty node");

              return trustedParty;
            });

        repository.findDefault()
            .doOnError(_ -> logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(System.out::println))
            .block();

        verify(client).findDefaultId();
        verify(client).findById(TRUSTED_PARTY_ID);
        trustedPartyFactory.verify(() -> TrustedPartyFactory.build(trustedPartyNode));
      }
    }
  }

  @Nested
  @DisplayName("findByOrganizationIdentifier")
  class FindByOrganizationIdentifier {
    @Test
    @DisplayName("Find default TrustedParty")
    void findByOrganizationIdentifier_findNodeAndBuildModel() {
      try (MockedStatic<TrustedPartyFactory> trustedPartyFactory = mockStatic(TrustedPartyFactory.class)) {

        when(client.findByOrganizationIdentifier(anyString())).thenAnswer((InvocationOnMock invocation) -> {
          String orgIdentifier = invocation.getArgument(0);
          assertEquals(ORGANIZATION_IDENTIFIER, orgIdentifier, "Neo4j cient method 'findByOrganizationIdentifier' should receive exact Organization identifier");

          return Flux.just(trustedPartyNode);
        });

        trustedPartyFactory.when(() -> TrustedPartyFactory.build(any(TrustedPartyNode.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              TrustedPartyNode node = invocation.getArgument(0);
              assertEquals(trustedPartyNode, node, "Factory should receive exact TrustedParty node");

              return trustedParty;
            });

        repository.findByOrganizationIdentifier(ORGANIZATION_IDENTIFIER)
            .doOnError(_ -> logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(System.out::println))
            .block();

        verify(client).findByOrganizationIdentifier(ORGANIZATION_IDENTIFIER);
        trustedPartyFactory.verify(() -> TrustedPartyFactory.build(trustedPartyNode));
      }
    }
  }

  @Nested
  @DisplayName("findUrlByOrganizationIdentifier")
  class FindUrlByOrganizationIdentifier {
    @Test
    @DisplayName("Find default TrustedParty")
    void findUrlByOrganizationIdentifier_findIdThenUrl() {
      when(client.findIdByOrganizationIdentifier(anyString())).thenAnswer((InvocationOnMock invocation) -> {
        String orgIdentifier = invocation.getArgument(0);
        assertEquals(ORGANIZATION_IDENTIFIER, orgIdentifier, "Neo4j cient method 'findIdByOrganizationIdentifier' should receive exact Organization identifier");

        return Flux.just(TRUSTED_PARTY_ID);
      });

      when(client.findUrlById(anyString())).thenAnswer((InvocationOnMock invocation) -> {
        String id = invocation.getArgument(0);
        assertEquals(TRUSTED_PARTY_ID, id, "Neo4j cient method 'findUrlById' should receive exact TrustedParty id");

        return Mono.just(TRUSTED_PARTY_URL);
      });

      repository.findUrlByOrganizationIdentifier(ORGANIZATION_IDENTIFIER)
          .doOnError(_ -> logAppender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .forEach(System.out::println))
          .block();

      verify(client).findIdByOrganizationIdentifier(ORGANIZATION_IDENTIFIER);
      verify(client).findUrlById(TRUSTED_PARTY_ID);
    }
  }
}
