package org.commonprovenance.framework.store.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.persistence.entity.DocumentEntity;
import org.commonprovenance.framework.store.persistence.impl.DocumentPersistenceImpl;
import org.commonprovenance.framework.store.persistence.repository.DocumentRepository;
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
  private final String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
  private final String FORMAT_1 = "JSON";

  private final String TEST_ID_2 = "dc3b1fc8-d01e-4405-8cf8-94320a11ba4c";
  private final String BASE64_STRING_GRAPH_2 = "AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAVwAAAG8AAAByAAAAbAAAAGQAAAAh";
  private final String FORMAT_2 = "JSON";

  @BeforeEach
  void setUp() {
    repository = new DocumentPersistenceImpl(documentRepository);
  }

  @Test
  @DisplayName("HappyPath - create - should create new entity in neo4j DB")
  void create_should_create_new_entity() {
    Document document = new Document(
        UUID.fromString(TEST_ID_1),
        BASE64_STRING_GRAPH_1,
        Format.from(FORMAT_1).get());

    when(documentRepository.save(any())).thenAnswer(invocation -> {
      DocumentEntity argumentEntity = invocation.getArgument(0);
      return Mono.just(argumentEntity);
    });

    StepVerifier.create(repository.create(document))
        .assertNext(doc -> {
          assertEquals(TEST_ID_1, doc.getId().toString());
          assertEquals(FORMAT_1, doc.getFormat().toString());
          assertEquals(BASE64_STRING_GRAPH_1, doc.getGraph());
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - create - should handle terminated IllegalArgumentException in case the given entity is null.")
  void create_should_handle_terminated_IllegalArgumentException() {
    String errMessage = "Wrong Argumnent..";
    Document document = new Document(
        UUID.fromString(TEST_ID_1),
        BASE64_STRING_GRAPH_1,
        Format.from(FORMAT_1).get());

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
        UUID.fromString(TEST_ID_1),
        BASE64_STRING_GRAPH_1,
        Format.from(FORMAT_1).get());

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
        UUID.fromString(TEST_ID_1),
        BASE64_STRING_GRAPH_1,
        Format.from(FORMAT_1).get());

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
          new DocumentEntity(TEST_ID_1, BASE64_STRING_GRAPH_1, FORMAT_1),
          new DocumentEntity(TEST_ID_2, BASE64_STRING_GRAPH_2, FORMAT_2));
    });

    StepVerifier.create(repository.getAll())
        .assertNext(doc -> {
          assertInstanceOf(Document.class, doc, "should return Document");

          assertEquals(TEST_ID_1, doc.getId().toString(), "should return document with exact id");
          assertEquals(BASE64_STRING_GRAPH_1, doc.getGraph(), "should return document with exact graph");
          assertEquals(FORMAT_1, doc.getFormat().toString(), "should return document with exact format");
        })
        .assertNext(doc -> {
          assertInstanceOf(Document.class, doc, "should return Document");

          assertEquals(TEST_ID_2, doc.getId().toString(), "should return document with exact id");
          assertEquals(BASE64_STRING_GRAPH_2, doc.getGraph(), "should return document with exact graph");
          assertEquals(FORMAT_2, doc.getFormat().toString(), "should return document with exact format");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("HappyPath - getById - should load entity from neo4j DB")
  void getById_should_load_entity() {
    when(documentRepository.findById(anyString())).thenAnswer(invocation -> {
      String id = invocation.getArgument(0);
      switch (id) {
        case TEST_ID_1:
          return Mono.just(new DocumentEntity(TEST_ID_1, BASE64_STRING_GRAPH_1, FORMAT_1));
        case TEST_ID_2:
          return Mono.just(new DocumentEntity(TEST_ID_2, BASE64_STRING_GRAPH_2, FORMAT_2));
        case null:
          return Mono.error(new IllegalArgumentException("Id can not be 'null'"));
        default:
          return Mono.empty();
      }
    });

    StepVerifier.create(repository.getById(UUID.fromString(TEST_ID_1)))
        .assertNext(doc -> {
          assertInstanceOf(Document.class, doc, "should return Document");

          assertEquals(TEST_ID_1, doc.getId().toString(), "should return document with exact id");
          assertEquals(BASE64_STRING_GRAPH_1, doc.getGraph(), "should return document with exact graph");
          assertEquals(FORMAT_1, doc.getFormat().toString(), "should return document with exact format");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("HappyPath - getById - should not load entity from neo4j DB")
  void getById_should_not_load__entity() {
    when(documentRepository.findById(anyString())).thenAnswer(invocation -> {
      String id = invocation.getArgument(0);
      switch (id) {
        case TEST_ID_1:
          return Mono.just(new DocumentEntity(TEST_ID_1, BASE64_STRING_GRAPH_1, FORMAT_1));
        case TEST_ID_2:
          return Mono.just(new DocumentEntity(TEST_ID_2, BASE64_STRING_GRAPH_2, FORMAT_2));
        case null:
          return Mono.error(new IllegalArgumentException("Id can not be 'null'"));
        default:
          return Mono.empty();
      }
    });

    StepVerifier.create(repository.getById(UUID.fromString("6f6fed6d-f5c3-44b3-bcca-db9453564122")))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - getById - should handle terminated IllegalArgumentException")
  void getById_should_handle_terminated_IllegalArgumentException() {
    StepVerifier.create(repository.getById((UUID) null))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(
              InternalApplicationException.class, err,
              "should be InternalApplicationException - Exception");
          assertEquals(
              "Identifier can not be 'null'!",
              err.getMessage(),
              "should have exact error message");

          assertInstanceOf(
              IllegalArgumentException.class, err.getCause(),
              "should be IllegalArgumentException - Exception cause");
          assertNull(err.getCause().getMessage(),
              "should have exact error message");
        });
  }

  @Test
  @DisplayName("HappyPath - deleteById - should delete entity from neo4j DB")
  void deleteById_should_delete_entity() {
    when(documentRepository.deleteById(anyString())).thenAnswer(invocation -> {
      String id = invocation.getArgument(0);
      if (id == null) {
        return Mono.error(new IllegalArgumentException("Id can not be 'null'"));
      } else {
        return Mono.empty().then();
      }
    });

    StepVerifier.create(repository.deleteById(UUID.fromString(TEST_ID_1)))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - deleteById - should handle terminated IllegalArgumentException")
  void deleteById_should_handle_terminated_IllegalArgumentException() {
    StepVerifier.create(repository.deleteById((UUID) null))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(
              InternalApplicationException.class, err,
              "should be InternalApplicationException - Exception");
          assertEquals(
              "Identifier can not be 'null'!",
              err.getMessage(),
              "should have exact error message");

          assertInstanceOf(
              IllegalArgumentException.class, err.getCause(),
              "should be IllegalArgumentException - Exception cause");
          assertNull(err.getCause().getMessage(),
              "should have exact error message");
        });
  }
}