package org.commonprovenance.framework.store.service.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.persistence.DocumentPersistence;
import org.commonprovenance.framework.store.service.persistence.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

@DisplayName("Service - DocumentServiceImpl UnitTest")

class DocumentServiceTest {
  private class DocumentRepositoryStub implements DocumentPersistence {
    final static String UUID_STR_1 = "e3cf8742-b595-47f4-8aae-a1e94b62a856";
    final static UUID UUID_1 = UUID.fromString(UUID_STR_1);
    final static String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    final static Format FORMAT_1 = Format.JSON;
    final static String SIGNATURE = "...";

    final static Document DOCUMENT_1 = new Document(UUID_1, BASE64_STRING_GRAPH_1, FORMAT_1, SIGNATURE);

    final static String UUID_STR_2 = "dc3b1fc8-d01e-4405-8cf8-94320a11ba4c";
    final static UUID UUID_2 = UUID.fromString(UUID_STR_2);
    final static String BASE64_STRING_GRAPH_2 = "AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAVwAAAG8AAAByAAAAbAAAAGQAAAAh";
    final static Format FORMAT_2 = Format.JSON;
    final static Document DOCUMENT_2 = new Document(UUID_2, BASE64_STRING_GRAPH_2, FORMAT_2, SIGNATURE);

    @Override
    public Mono<Document> create(Document document) {
      return document == null
          ? Mono.error(new InternalApplicationException("Illegal argument!",
              new IllegalArgumentException("Document can not be 'null'!")))
          : Mono.just(document);
    }

    @Override
    public Flux<Document> getAll() {
      return Flux.just(DOCUMENT_1, DOCUMENT_2);
    }

    @Override
    public Mono<Document> getById(UUID uuid) {
      if (uuid == null)
        return Mono.error(new InternalApplicationException(
            "DocumentNeo4jRepository - Error while reading document",
            new IllegalArgumentException(
                "Id can not be 'null'!")));

      switch (uuid.toString()) {
        case UUID_STR_1:
          return Mono.just(DOCUMENT_1);
        case UUID_STR_2:
          return Mono.just(DOCUMENT_2);
        default:
          return Mono.empty();
      }
    }

    @Override
    public Mono<Void> deleteById(UUID uuid) {
      return Mono.empty().then();
    }

  }

  private final DocumentPersistence documentRepository;

  private DocumentServiceImpl documentService;

  public DocumentServiceTest() {
    this.documentRepository = new DocumentRepositoryStub();
  }

  @BeforeEach
  void setUp() {
    documentService = new DocumentServiceImpl(documentRepository);
  }

  @Test
  @DisplayName("HappyPath - storeDocument - should return new Document which has been stored.")
  void storeDocument_return_new_document() {

    StepVerifier.create(documentService.storeDocument(DocumentRepositoryStub.DOCUMENT_1))
        .assertNext(doc -> {
          assertEquals(
              DocumentRepositoryStub.UUID_STR_1, doc.getId().map(UUID::toString).orElse("?uuid?"),
              "should have exact Id");
          assertEquals(DocumentRepositoryStub.BASE64_STRING_GRAPH_1, doc.getGraph(),
              "should have exact graph");
          assertEquals(
              DocumentRepositoryStub.FORMAT_1.toString(), doc.getFormat().map(Format::toString).orElse("?format?"),
              "should have exact format");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - storeDocument - should propagate exception from repository, if any.")
  void storeDocument_propagete_exception_from_repository() {
    StepVerifier.create(documentService.storeDocument(null))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(InternalApplicationException.class,
              err,
              "should be InternalApplicationException - Exception");
          assertEquals(
              "Illegal argument!",
              err.getMessage(),
              "should have exact error message");

          assertInstanceOf(
              IllegalArgumentException.class,
              err.getCause(),
              "should be NullPointerException - Exception cause");
          assertEquals(
              "Document can not be 'null'!",
              err.getCause().getMessage(),
              "should have exact error message");
        });
  }

  @Test
  @DisplayName("HappyPath - getAllDocuments - should return Flux with all documents from repository.")
  void getAllDocuments_should_return_all_documents() {

    StepVerifier.create(documentService.getAllDocuments())
        .assertNext(doc -> {
          assertEquals(DocumentRepositoryStub.UUID_STR_1, doc.getId().map(UUID::toString).orElse("?uuid?"),
              "should have exact id");
        })
        .assertNext(doc -> {
          assertEquals(DocumentRepositoryStub.UUID_STR_2, doc.getId().map(UUID::toString).orElse("?uuid?"),
              "should have exact id");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("HappyPath - getDocumentById - should return Mono with exact document from repository.")
  void getDocumentById_should_return_mono_with_exact_document() {

    StepVerifier.create(documentService.getDocumentById(DocumentRepositoryStub.UUID_1))
        .assertNext(doc -> {
          assertEquals(DocumentRepositoryStub.UUID_STR_1, doc.getId().map(UUID::toString).orElse("?uuid?"),
              "should have exact id");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("HappyPath - getDocumentById - should return empty Mono.")
  void getDocumentById_should_return_empty_mono() {

    StepVerifier.create(documentService.getDocumentById(UUID.randomUUID()))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - getDocumentById - should propagate exception from repository, if any.")
  void getDocumentById_propagete_exception_from_repository() {

    StepVerifier.create(documentService.getDocumentById(null))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(InternalApplicationException.class,
              err,
              "should be InternalApplicationException - Exception");
          assertEquals(
              "DocumentNeo4jRepository - Error while reading document",
              err.getMessage(),
              "should have exact error message");

          assertInstanceOf(
              IllegalArgumentException.class,
              err.getCause(),
              "should be NullPointerException - Exception cause");
          assertEquals(
              "Id can not be 'null'!",
              err.getCause().getMessage(),
              "should have exact error message");
        });
  }

  @Test
  @DisplayName("HappyPath - deleteById - should return empty Mono.")

  void deleteDocumentById_should_delete_document() {

    StepVerifier.create(documentService.deleteDocumentById(DocumentRepositoryStub.UUID_1))
        .verifyComplete();
  }
}