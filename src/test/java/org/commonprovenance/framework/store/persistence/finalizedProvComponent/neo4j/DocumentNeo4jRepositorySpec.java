package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.factory.DocumentFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.DocumentNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client.DocumentNeo4jRepositoryClient;
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
@DisplayName("DocumentRepository Spec")
class DocumentNeo4jRepositorySpec {
  private final String DOCUMENT_IDENTIFIER = "a45e5ccb-c486-4f77-8655-b4de9a2bac00";
  private final String ORGANIZATION_IDENTIFIER = "a87ec42f-cd1e-4640-8617-ddd1b962770c";
  private final String DOCUMENT_ID = "4:c07ee6e7-57c3-477f-9201-b05fab4c0316:71";

  @Mock
  private DocumentNeo4jRepositoryClient client;

  @Mock
  private Document document;

  @Mock
  private DocumentNode documentNode;

  private DocumentRepository repository;

  private ListAppender<ILoggingEvent> logAppender;
  private Logger storeWebLogger;

  @BeforeEach
  void setUp() {
    repository = new DocumentNeo4jRepository(client);
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
    @DisplayName("Save new DocumentNode into neo4j database")
    void save_buildsNodeAndCallSave() {
      try (MockedStatic<DocumentNodeFactory> documentNodeFactory = mockStatic(DocumentNodeFactory.class)) {

        documentNodeFactory.when(() -> DocumentNodeFactory.buildWithRelations(any(Document.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              Document doc = invocation.getArgument(0);
              assertEquals(document, doc, "Factory should receive exact document");

              return documentNode;
            });

        when(client.save(any(DocumentNode.class))).thenAnswer((InvocationOnMock invocation) -> {
          DocumentNode node = invocation.getArgument(0);
          assertEquals(documentNode, node, "Neo4j cient should receive exact document node");
          return Mono.just(node);
        });

        repository.save(document)
            .doOnError(_ -> logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(System.out::println))
            .block();

        documentNodeFactory.verify(() -> DocumentNodeFactory.buildWithRelations(document));
        verify(client).save(documentNode);
      }
    }
  }

  @Nested
  @DisplayName("findByIdentifier")
  class FindByIdentifier {
    @Test
    @DisplayName("Find DocumentNode by its identifier")
    void findByIdentifier_findIdThenNodeAndBuildDocument() {
      try (MockedStatic<DocumentFactory> documentFactory = mockStatic(DocumentFactory.class)) {

        when(client.getIdByIdentifier(anyString())).thenAnswer((InvocationOnMock invocation) -> {
          String identifier = invocation.getArgument(0);
          assertEquals(DOCUMENT_IDENTIFIER, identifier, "Neo4j cient method 'getIdByIdentifier' should receive exact document identifier");
          return Flux.just(DOCUMENT_ID);
        });

        when(client.findById(anyString())).thenAnswer((InvocationOnMock invocation) -> {
          String id = invocation.getArgument(0);
          assertEquals(DOCUMENT_ID, id, "Neo4j cient method 'findById' should receive exact document node id");
          return Mono.just(documentNode);
        });

        documentFactory.when(() -> DocumentFactory.buildWithRelations(any(DocumentNode.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              DocumentNode node = invocation.getArgument(0);
              assertEquals(documentNode, node, "Factory should receive exact document node");

              return Either.right(document);
            });

        repository.findByIdentifier(DOCUMENT_IDENTIFIER)
            .doOnError(_ -> logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(System.out::println))
            .block();

        verify(client).getIdByIdentifier(DOCUMENT_IDENTIFIER);
        verify(client).findById(DOCUMENT_ID);
        documentFactory.verify(() -> DocumentFactory.buildWithRelations(documentNode));
      }
    }
  }

  @Nested
  @DisplayName("existsByIdentifier")
  class ExistsByIdentifier {
    @Test
    @DisplayName("Check if DocumentNode exists")
    void existsByIdentifier_countNodes() {

      when(client.countByIdentifier(anyString())).thenAnswer((InvocationOnMock invocation) -> {
        String identifier = invocation.getArgument(0);
        assertEquals(DOCUMENT_IDENTIFIER, identifier, "Neo4j cient method 'countByIdentifier' should receive exact document identifier");
        return Mono.just(1);
      });

      repository.existsByIdentifier(DOCUMENT_IDENTIFIER)
          .doOnError(_ -> logAppender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .forEach(System.out::println))
          .block();

      verify(client).countByIdentifier(DOCUMENT_IDENTIFIER);
    }
  }

  @Nested
  @DisplayName("getOrganizationIdentifierByIdentifier")
  class GetOrganizationIdentifierByIdentifier {
    @Test
    @DisplayName("Get Organization identifier by Document identifier")
    void getOrganizationIdentifierByIdentifier_getOrganizationIdentifier() {

      when(client.findOrganizationIdentifierByIdentifier(anyString())).thenAnswer((InvocationOnMock invocation) -> {
        String identifier = invocation.getArgument(0);
        assertEquals(DOCUMENT_IDENTIFIER, identifier, "Neo4j cient method 'findOrganizationIdentifierByIdentifier' should receive exact document identifier");
        return Flux.just(ORGANIZATION_IDENTIFIER);
      });

      repository.getOrganizationIdentifierByIdentifier(DOCUMENT_IDENTIFIER)
          .doOnError(_ -> logAppender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .forEach(System.out::println))
          .block();

      verify(client).findOrganizationIdentifierByIdentifier(DOCUMENT_IDENTIFIER);
    }
  }
}
