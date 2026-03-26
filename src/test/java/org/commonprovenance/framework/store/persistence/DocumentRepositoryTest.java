package org.commonprovenance.framework.store.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.impl.DocumentPersistenceImpl;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("Neo4j Repository - DocumentNeo4jRepository UnitTest")
class DocumentRepositoryTest {

  @Mock
  private DocumentRepository documentRepository;

  private DocumentPersistenceImpl repository;

  private final String TEST_ID_1 = "e3cf8742-b595-47f4-8aae-a1e94b62a856";
  private final String TEST_ORG_ID_1 = "6ee9d79b-0615-4cb1-b0f3-2303d10c8cff";
  private final String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
  private final String FORMAT_1 = "JSON";

  private final String TEST_ID_2 = "dc3b1fc8-d01e-4405-8cf8-94320a11ba4c";
  private final String BASE64_STRING_GRAPH_2 = "AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAVwAAAG8AAAByAAAAbAAAAGQAAAAh";
  private final String FORMAT_2 = "JSON";
  private final String SIGNATURE = "...";

  @BeforeEach
  void setUp() {
    repository = new DocumentPersistenceImpl(documentRepository);
  }

  @Test
  @DisplayName("HappyPath - create - should create new entity in neo4j DB")
  void create_should_create_new_entity() {
    Document document = new Document(
        TEST_ID_1,
        TEST_ORG_ID_1,
        BASE64_STRING_GRAPH_1,
        Format.from(FORMAT_1).get(),
        SIGNATURE);

    when(documentRepository.save(any())).thenAnswer(invocation -> {
      DocumentNode argumentEntity = invocation.getArgument(0);
      return Mono.just(argumentEntity);
    });

    StepVerifier.create(repository.create(document))
        .assertNext(doc -> {
          assertEquals(Optional.of(TEST_ID_1), doc.getIdentifier());
          assertEquals(FORMAT_1, doc.getFormat().map(Format::toString).orElse("?format?"));
          assertEquals(BASE64_STRING_GRAPH_1, doc.getGraph());
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - create - should handle terminated IllegalArgumentException in case the given entity is null.")
  void create_should_handle_terminated_IllegalArgumentException() {
    String errMessage = "Wrong Argumnent..";
    Document document = new Document(
        TEST_ID_1,
        TEST_ORG_ID_1,
        BASE64_STRING_GRAPH_1,
        Format.from(FORMAT_1).get(),
        SIGNATURE);

    when(documentRepository.save(any())).thenAnswer(_invocation -> {
      return Mono.error(new IllegalArgumentException(errMessage));
    });

    StepVerifier.create(repository.create(document))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(InternalApplicationException.class, err,
              "should be InternalApplicationException - Exception");
          assertEquals("DocumentNeo4jRepository - Error while creating new Document", err.getMessage(),
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
    Document document = new Document(
        TEST_ID_1,
        TEST_ORG_ID_1,
        BASE64_STRING_GRAPH_1,
        Format.from(FORMAT_1).get(),
        SIGNATURE);

    when(documentRepository.save(any())).thenAnswer(_invocation -> {
      return Mono.error(new OptimisticLockingFailureException(errMessage));
    });

    StepVerifier.create(repository.create(document))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(InternalApplicationException.class, err,
              "should be InternalApplicationException - Exception");
          assertEquals("DocumentNeo4jRepository - Error while creating new Document", err.getMessage(),
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
    Document document = new Document(
        TEST_ID_1,
        TEST_ORG_ID_1,
        BASE64_STRING_GRAPH_1,
        Format.from(FORMAT_1).get(),
        SIGNATURE);

    when(documentRepository.save(any())).thenAnswer(_invocation -> {
      throw new IllegalArgumentException(errMessage);
    });

    StepVerifier.create(repository.create(document))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(InternalApplicationException.class, err,
              "should be InternalApplicationException - Exception");
          assertEquals("DocumentNeo4jRepository - Error while creating new Document", err.getMessage(),
              "should have exact error message");

          assertInstanceOf(IllegalArgumentException.class, err.getCause(),
              "should be IllegalArgumentException - Exception cause");
          assertEquals(errMessage, err.getCause().getMessage(),
              "should have exact error message");
        });
  }

  @Test
  @DisplayName("HappyPath - getAll - should load all entities from neo4j DB")
  void getAll_should_load_all_entities() {
    when(documentRepository.findAll()).thenAnswer(_invocation -> {
      return Flux.just(
          new DocumentNode(TEST_ID_1, BASE64_STRING_GRAPH_1, FORMAT_1),
          new DocumentNode(TEST_ID_2, BASE64_STRING_GRAPH_2, FORMAT_2));
    });

    StepVerifier.create(repository.getAll())
        .assertNext(doc -> {
          assertInstanceOf(Document.class, doc, "should return Document");

          assertEquals(Optional.of(TEST_ID_1), doc.getIdentifier(),
              "should return document with exact id");
          assertEquals(BASE64_STRING_GRAPH_1, doc.getGraph(), "should return document with exact graph");
          assertEquals(FORMAT_1, doc.getFormat().map(Format::toString).orElse("?format?"),
              "should return document with exact format");
        })
        .assertNext(doc -> {
          assertInstanceOf(Document.class, doc, "should return Document");

          assertEquals(Optional.of(TEST_ID_2), doc.getIdentifier(),
              "should return document with exact id");
          assertEquals(BASE64_STRING_GRAPH_2, doc.getGraph(), "should return document with exact graph");
          assertEquals(FORMAT_2, doc.getFormat().map(Format::toString).orElse("?format?"),
              "should return document with exact format");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("HappyPath - getByIdentifier - should load entity from neo4j DB")
  void getByIdentifier_should_load_entity() {
    when(documentRepository.findByIdentifier(anyString())).thenAnswer(invocation -> {
      String id = invocation.getArgument(0);
      switch (id) {
        case TEST_ID_1:
          return Mono.just(new DocumentNode(TEST_ID_1, BASE64_STRING_GRAPH_1, FORMAT_1));
        case TEST_ID_2:
          return Mono.just(new DocumentNode(TEST_ID_2, BASE64_STRING_GRAPH_2, FORMAT_2));
        case null:
          return Mono.error(new IllegalArgumentException("Id can not be 'null'"));
        default:
          return Mono.empty();
      }
    });

    StepVerifier.create(repository.getByIdentifier(TEST_ID_1))
        .assertNext(doc -> {
          assertInstanceOf(Document.class, doc, "should return Document");

          assertEquals(Optional.of(TEST_ID_1), doc.getIdentifier(),
              "should return document with exact id");
          assertEquals(BASE64_STRING_GRAPH_1, doc.getGraph(), "should return document with exact graph");
          assertEquals(FORMAT_1, doc.getFormat().map(Format::toString).orElse("?format?"),
              "should return document with exact format");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("HappyPath - getByIdentifier - should not load entity from neo4j DB")
  void getById_should_not_load__entity() {
    when(documentRepository.findByIdentifier(anyString())).thenAnswer(invocation -> {
      String id = invocation.getArgument(0);
      switch (id) {
        case TEST_ID_1:
          return Mono.just(new DocumentNode(TEST_ID_1, BASE64_STRING_GRAPH_1, FORMAT_1));
        case TEST_ID_2:
          return Mono.just(new DocumentNode(TEST_ID_2, BASE64_STRING_GRAPH_2, FORMAT_2));
        case null:
          return Mono.error(new IllegalArgumentException("Id can not be 'null'"));
        default:
          return Mono.empty();
      }
    });

    StepVerifier.create(repository.getByIdentifier("6f6fed6d-f5c3-44b3-bcca-db9453564122"))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(ApplicationException.class, err);
          assertInstanceOf(NotFoundException.class, err);
          assertEquals(err.getMessage(),
              "Document with identifier '6f6fed6d-f5c3-44b3-bcca-db9453564122' has not been found!");
        });
  }

  @Test
  @DisplayName("ErrorPath - getByIdentifier - should handle terminated IllegalArgumentException")
  void getByIdentifier_should_handle_terminated_IllegalArgumentException() {
    StepVerifier.create(repository.getByIdentifier((String) null))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(
              InternalApplicationException.class, err,
              "should be InternalApplicationException - Exception");
          assertEquals(
              "Document identifier can not be 'null'!",
              err.getMessage(),
              "should have exact error message");

          assertNull(err.getCause());
          // assertInstanceOf(
          // IllegalArgumentException.class, err.getCause(),
          // "should be IllegalArgumentException - Exception cause");
          // assertNull(err.getCause().getMessage(),
          // "should have exact error message");
        });
  }

}