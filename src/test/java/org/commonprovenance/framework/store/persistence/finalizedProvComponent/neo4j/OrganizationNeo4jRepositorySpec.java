package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.OrganizationFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.OrganizationRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.OrganizationNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client.OrganizationNeo4jRepositoryClient;
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
@DisplayName("OrganizationRepository Spec")
class OrganizationNeo4jRepositorySpec {
  private final String DOCUMENT_IDENTIFIER = "a45e5ccb-c486-4f77-8655-b4de9a2bac00";
  private final String ORGANIZATION_IDENTIFIER = "a87ec42f-cd1e-4640-8617-ddd1b962770c";
  private final String TRUSTED_PARTY_ID = "TrustedParty";
  private final String ORGANIZATION_CERT_CLIENT = "client";
  private final String ORGANIZATION_CERT_INT1 = "int_1";
  private final String ORGANIZATION_CERT_INT2 = "int_2";

  @Mock
  private OrganizationNeo4jRepositoryClient client;

  @Mock
  private Organization organization;

  @Mock
  private TrustedParty trustedParty;

  @Mock
  private Document document;

  @Mock
  private OrganizationNode organizationNode;

  private OrganizationRepository repository;

  private ListAppender<ILoggingEvent> logAppender;
  private Logger storeWebLogger;

  @BeforeEach
  void setUp() {
    repository = new OrganizationNeo4jRepository(client);
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
    @DisplayName("Save new Organization into neo4j database")
    void save_buildsNodeAndCallSave() {
      try (MockedStatic<OrganizationNodeFactory> organizationNodeFactory = mockStatic(OrganizationNodeFactory.class)) {

        organizationNodeFactory.when(() -> OrganizationNodeFactory.build(any(Organization.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              Organization org = invocation.getArgument(0);
              assertEquals(organization, org, "Factory should receive exact organization");

              return organizationNode;
            });

        when(client.save(any(OrganizationNode.class))).thenAnswer((InvocationOnMock invocation) -> {
          OrganizationNode node = invocation.getArgument(0);
          assertEquals(organizationNode, node, "Neo4j cient should receive exact organization node");
          return Mono.just(node);
        });

        repository.save(organization)
            .doOnError(_ -> logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(System.out::println))
            .block();

        organizationNodeFactory.verify(() -> OrganizationNodeFactory.build(organization));
        verify(client).save(organizationNode);
      }
    }
  }

  @Nested
  @DisplayName("update")
  class Update {
    @Test
    @DisplayName("Update existing Organization in neo4j database")
    void update_updateCertificates() {
      when(organization.getIdentifier()).thenReturn(ORGANIZATION_IDENTIFIER);
      when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT_CLIENT);
      when(organization.getIntermediateCertificates()).thenReturn(List.of(ORGANIZATION_CERT_INT1, ORGANIZATION_CERT_INT2));

      when(client.updateCertificates(anyString(), anyString(), anyList())).thenAnswer((InvocationOnMock invocation) -> {
        String identifier = invocation.getArgument(0);
        String clientCert = invocation.getArgument(1);
        List<String> intermediateCerts = invocation.getArgument(2);

        assertEquals(ORGANIZATION_IDENTIFIER, identifier, "Neo4j cient should receive exact organization identifier");
        assertEquals(ORGANIZATION_CERT_CLIENT, clientCert, "Neo4j cient should receive exact client certificate");
        assertIterableEquals(List.of(ORGANIZATION_CERT_INT1, ORGANIZATION_CERT_INT2), intermediateCerts, "Neo4j cient should receive exact intermediate certs");

        return Mono.just(true);
      });

      repository.update(organization)
          .doOnError(_ -> logAppender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .forEach(System.out::println))
          .block();

      verify(client).updateCertificates(ORGANIZATION_IDENTIFIER, ORGANIZATION_CERT_CLIENT, List.of(ORGANIZATION_CERT_INT1, ORGANIZATION_CERT_INT2));
    }
  }

  @Nested
  @DisplayName("connectTrusts")
  class ConnectTrusts {
    @Test
    @DisplayName("Create Trusts relation from Organization to TrustedParty")
    void connectTrusts_createTrustsRelationship() {
      when(organization.getIdentifier()).thenReturn(ORGANIZATION_IDENTIFIER);
      when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));

      when(trustedParty.getId()).thenReturn(TRUSTED_PARTY_ID);

      when(client.createTrustsRelationship(anyString(), anyString())).thenAnswer((InvocationOnMock invocation) -> {
        String identifier = invocation.getArgument(0);
        String tpId = invocation.getArgument(1);

        assertEquals(ORGANIZATION_IDENTIFIER, identifier, "Neo4j cient should receive exact Organization identifier");
        assertEquals(TRUSTED_PARTY_ID, tpId, "Neo4j cient should receive exact TrustedParty id");

        return Mono.just(true);
      });

      repository.connectTrusts(organization)
          .doOnError(_ -> logAppender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .forEach(System.out::println))
          .block();

      verify(client).createTrustsRelationship(ORGANIZATION_IDENTIFIER, TRUSTED_PARTY_ID);
    }
  }

  @Nested
  @DisplayName("findByIdentifier")
  class FindByIdentifier {
    @Test
    @DisplayName("Find OrganizationNode by its identifier")
    void findByIdentifier_findNodeAndBuildOrganization() {
      try (MockedStatic<OrganizationFactory> organizationFactory = mockStatic(OrganizationFactory.class)) {

        when(client.getByIdentifier(anyString())).thenAnswer((InvocationOnMock invocation) -> {
          String identifier = invocation.getArgument(0);
          assertEquals(ORGANIZATION_IDENTIFIER, identifier, "Neo4j cient method 'getByIdentifier' should receive exact organization identifier");
          return Flux.just(organizationNode);
        });

        organizationFactory.when(() -> OrganizationFactory.build(any(OrganizationNode.class)))
            .thenAnswer((InvocationOnMock invocation) -> {
              OrganizationNode node = invocation.getArgument(0);
              assertEquals(organizationNode, node, "Factory should receive exact organization node");

              return organization;
            });

        repository.findByIdentifier(ORGANIZATION_IDENTIFIER)
            .doOnError(_ -> logAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .forEach(System.out::println))
            .block();

        verify(client).getByIdentifier(ORGANIZATION_IDENTIFIER);
        organizationFactory.verify(() -> OrganizationFactory.build(organizationNode));
      }
    }
  }

  @Nested
  @DisplayName("existsByIdentifier")
  class ExistsByIdentifier {
    @Test
    @DisplayName("Check if OrganizationNode exists")
    void existsByIdentifier_countNodes() {

      when(client.countByIdentifier(anyString())).thenAnswer((InvocationOnMock invocation) -> {
        String identifier = invocation.getArgument(0);
        assertEquals(ORGANIZATION_IDENTIFIER, identifier, "Neo4j cient method 'countByIdentifier' should receive exact organization identifier");
        return Mono.just(1);
      });

      repository.existsByIdentifier(ORGANIZATION_IDENTIFIER)
          .doOnError(_ -> logAppender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .forEach(System.out::println))
          .block();

      verify(client).countByIdentifier(ORGANIZATION_IDENTIFIER);
    }
  }

  @Nested
  @DisplayName("connectOwns")
  class ConnectOwns {
    @Test
    @DisplayName("Create Owns relation from Organization to Document")
    void connectOwns_createOwnsRelationship() {
      when(document.getIdentifier()).thenReturn(Either.right(DOCUMENT_IDENTIFIER));

      when(client.createOwnsRelationship(anyString(), anyString())).thenAnswer((InvocationOnMock invocation) -> {
        String identifier = invocation.getArgument(0);
        String docIdentifier = invocation.getArgument(1);

        assertEquals(ORGANIZATION_IDENTIFIER, identifier, "Neo4j cient should receive exact Organization identifier");
        assertEquals(DOCUMENT_IDENTIFIER, docIdentifier, "Neo4j cient should receive exact Document identifier");

        return Mono.just(true);
      });

      repository.connectOwns(ORGANIZATION_IDENTIFIER)
          .apply(document)
          .doOnError(_ -> logAppender.list.stream()
              .map(ILoggingEvent::getFormattedMessage)
              .forEach(System.out::println))
          .block();

      verify(client).createOwnsRelationship(ORGANIZATION_IDENTIFIER, DOCUMENT_IDENTIFIER);
    }
  }

}
