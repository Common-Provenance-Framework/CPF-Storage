package org.commonprovenance.framework.store.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.HasToken;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation.WasIssuedBy;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.DocumentNeo4jRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client.DocumentNeo4jRepositoryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openprovenance.prov.template.compiler.sql.Collections;
import org.openprovenance.prov.vanilla.QualifiedName;
import org.springframework.dao.OptimisticLockingFailureException;

import cz.muni.fi.cpm.model.CpmDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("Neo4j Repository - DocumentPersistence UnitTest")
class DocumentRepositoryTest {

  @Mock
  private DocumentNeo4jRepositoryClient client;

  @Mock
  private CpmDocument cpmDocument_01;

  @Mock
  private CpmDocument cpmDocument_02;

  @Mock
  private Token token_01;

  @Mock
  private Token token_02;

  private DocumentRepository repository;

  private final String TEST_ID_1 = "1:1b8970fc-3b2f-4159-bd97-a14b23027114:1";
  private final String TEST_IDENTIFIER_1 = "e3cf8742-b595-47f4-8aae-a1e94b62a856";
  private final String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
  private final Format FORMAT_1 = Format.JSON;

  private final String TEST_ID_2 = "\"1:fc7cacb3-f151-43d3-aec2-46e4df17e04f:2\"";
  private final String TEST_IDENTIFIER_2 = "dc3b1fc8-d01e-4405-8cf8-94320a11ba4c";
  private final String BASE64_STRING_GRAPH_2 = "AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAVwAAAG8AAAByAAAAbAAAAGQAAAAh";
  private final Format FORMAT_2 = Format.JSON;

  Document document_01;
  Document document_02;

  @BeforeEach
  void setUp() {
    repository = new DocumentNeo4jRepository(client);

    document_01 = new Document(
        BASE64_STRING_GRAPH_1,
        FORMAT_1,
        cpmDocument_01,
        token_01);

    document_02 = new Document(
        BASE64_STRING_GRAPH_2,
        FORMAT_2,
        cpmDocument_02,
        token_02);

  }

  @Test
  @DisplayName("HappyPath - create - should create new entity in neo4j DB")
  void create_should_create_new_entity() {

    when(client.save(any())).thenAnswer(invocation -> {
      DocumentNode argumentEntity = invocation.getArgument(0);
      return Mono.just(argumentEntity);
    });

    when(cpmDocument_01.getBundleId()).thenAnswer(invocation -> {
      return new QualifiedName("http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/", TEST_ID_1, "storage");
    });

    StepVerifier.create(repository.save(document_01))
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - create - should handle terminated IllegalArgumentException in case the given entity is null.")
  void create_should_handle_terminated_IllegalArgumentException() {
    String errMessage = "Wrong Argumnent..";

    when(client.save(any())).thenAnswer(_invocation -> {
      return Mono.error(new IllegalArgumentException(errMessage));
    });

    when(cpmDocument_01.getBundleId()).thenAnswer(invocation -> {
      return new QualifiedName("http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/", TEST_ID_1, "storage");
    });

    StepVerifier.create(repository.save(document_01))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(InternalApplicationException.class, err,
              "should be InternalApplicationException - Exception");
          assertEquals("Document has not been saved into DB!", err.getMessage(),
              "should have exact error message");

          assertInstanceOf(IllegalArgumentException.class, err.getCause(),
              "should be IllegalArgumentException - Exception cause");
          assertEquals(errMessage, err.getCause().getMessage(),
              "should have exact error message");
        });
  }

  @Test
  @DisplayName("ErrorPath - create - should handle terminated OptimisticLockingFailureException when the entity uses optimistic locking.")
  void create_should_handle_terminated_OptimisticLockingFailureException() {
    String errMessage = "Optimistic Locking..";

    when(client.save(any())).thenAnswer(_invocation -> {
      return Mono.error(new OptimisticLockingFailureException(errMessage));
    });

    when(cpmDocument_01.getBundleId()).thenAnswer(invocation -> {
      return new QualifiedName("http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/", TEST_ID_1, "storage");
    });

    StepVerifier.create(repository.save(document_01))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(InternalApplicationException.class, err,
              "should be InternalApplicationException - Exception");
          assertEquals("Document has not been saved into DB!", err.getMessage(),
              "should have exact error message");

          assertInstanceOf(
              OptimisticLockingFailureException.class, err.getCause(),
              "should be OptimisticLockingFailureException - Exception cause");
          assertEquals(errMessage, err.getCause().getMessage(),
              "should have exact error message");
        });
  }

  @Test
  @DisplayName("ErrorPath - create - should handle thrown IllegalArgumentException")
  void create_should_handle_thrown_IllegalArgumentException() {
    String errMessage = "Wrong Argumnent..";

    when(client.save(any())).thenAnswer(_invocation -> {
      throw new IllegalArgumentException(errMessage);
    });

    when(cpmDocument_01.getBundleId()).thenAnswer(invocation -> {
      return new QualifiedName("http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/", TEST_ID_1, "storage");
    });

    StepVerifier.create(repository.save(document_01))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(InternalApplicationException.class, err,
              "should be InternalApplicationException - Exception");
          assertEquals("Document has not been saved into DB!", err.getMessage(),
              "should have exact error message");

          assertInstanceOf(IllegalArgumentException.class, err.getCause(),
              "should be IllegalArgumentException - Exception cause");
          assertEquals(errMessage, err.getCause().getMessage(),
              "should have exact error message");
        });
  }

  @Test
  @DisplayName("HappyPath - getByIdentifier - should load entity from neo4j DB")
  void getByIdentifier_should_load_entity() {
    when(client.getIdByIdentifier(anyString())).thenAnswer(invocation -> {
      String identifier = invocation.getArgument(0);
      switch (identifier) {
        case TEST_IDENTIFIER_1:
          return Flux.just(TEST_ID_1);
        case TEST_IDENTIFIER_2:
          return Flux.just(TEST_ID_2);
        case null:
          return Flux.error(new IllegalArgumentException("Id can not be 'null'"));
        default:
          return Flux.empty();
      }
    });

    when(client.findById(anyString())).thenAnswer(invocation -> {
      String id = invocation.getArgument(0);
      TrustedPartyNode tpNode = new TrustedPartyNode(
          "1:20c19b3c-cbf6-4b52-bf56-1b1d4aa6da9a3:21",
          "TrustedParty",
          "cert_tp",
          "trusted-party:8020",
          true, true, true);

      DocumentNode node_01 = new DocumentNode(TEST_ID_1, TEST_IDENTIFIER_1, BASE64_STRING_GRAPH_1, FORMAT_1.toString(), Collections.emptyListOf(HasToken.class))
          .withToken(new TokenNode(
              "1:20c19b3c-cbf6-4b52-bf56-1b1d4aa6da9a3:11",
              "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJUcnVzdGVkX1BhcnR5IiwiZG9jX2lhdCI6MTc4MTI2OTk3MiwiZG9jX2RpZ2VzdCI6IjAyYjNiZDhiOTVmZWRkNzY3N2U5ZDBkZjZhZWZmYzhmOWM0NmE5YjM1ODcwYmRjNjlhYmY4MmJiNDFjOGIwMmQiLCJvcmdfaWQiOiI2ZmIyOTJhYS1lZTM4LTQ4YWUtOTk4Zi0wNzlhZDlkMDFlN2MiLCJpYXQiOjE3ODEyNjk5NzJ9.v4hZbyNENtAFCY8lYzjLmAs9Hi9Wbh0REYM6l32Ufvc",
              Collections.emptyListOf(WasIssuedBy.class))
              .withTrustedParty(tpNode));

      DocumentNode node_02 = new DocumentNode(TEST_ID_2, TEST_IDENTIFIER_2, BASE64_STRING_GRAPH_2, FORMAT_2.toString(), Collections.emptyListOf(HasToken.class))
          .withToken(new TokenNode(
              "1:20c19b3c-cbf6-4b52-bf56-1b1d4aa6da9a3:12",
              "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJUcnVzdGVkX1BhcnR5IiwiZG9jX2lhdCI6MTc4MTI2OTk3MiwiZG9jX2RpZ2VzdCI6IjAyYjNiZDhiOTVmZWRkNzY3N2U5ZDBkZjZhZWZmYzhmOWM0MWE5YjM1ODcwYmRjNjlhYmY4MmJiNDFjOGIwNGQiLCJvcmdfaWQiOiI2ZmIyOTJhYS1lZTM4LTQ4YWUtOTk4Zi0wNzlhZDlkMDFlN2MiLCJpYXQiOjE3ODEyNjk5NzJ9.Ylle4jCE2ngVbw0P-Qh_IC6o0Vj3Ab8pYa-YuGvsnXA",
              Collections.emptyListOf(WasIssuedBy.class))
              .withTrustedParty(tpNode));
      switch (id) {
        case TEST_ID_1:
          return Mono.just(node_01);
        case TEST_ID_2:
          return Mono.just(node_02);
        case null:
          return Mono.error(new IllegalArgumentException("Id can not be 'null'"));
        default:
          return Mono.empty();
      }
    });

    StepVerifier.create(repository.findByIdentifier(TEST_IDENTIFIER_1))
        .assertNext(doc -> {
          assertInstanceOf(Document.class, doc, "should return Document");

          // assertEquals(Optional.of(TEST_IDENTIFIER_1), doc.getIdentifier(),
          // "should return document with exact id");
          assertEquals(BASE64_STRING_GRAPH_1, doc.getGraph(), "should return document with exact graph");
          assertEquals(FORMAT_1, doc.getFormat(),
              "should return document with exact format");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("HappyPath - getByIdentifier - should not load entity from neo4j DB")
  void getById_should_not_load__entity() {
    when(client.getIdByIdentifier(anyString())).thenAnswer(_ -> {
      return Flux.empty();
    });

    StepVerifier.create(repository.findByIdentifier("6f6fed6d-f5c3-44b3-bcca-db9453564122")).verifyErrorSatisfies(err ->

    {
      assertInstanceOf(ApplicationException.class, err);
      assertInstanceOf(NotFoundException.class, err);
      assertEquals(err.getMessage(),
          "Document with identifier '6f6fed6d-f5c3-44b3-bcca-db9453564122' has not been found!");
    });
  }

}
