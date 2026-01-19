package org.commonprovenance.framework.storage.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.commonprovenance.framework.storage.exceptions.InternalApplicationException;
import org.commonprovenance.framework.storage.model.Document;
import org.commonprovenance.framework.storage.model.Format;
import org.commonprovenance.framework.storage.persistence.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Service - DocumentServiceImpl")

class DocumentServiceImplTest {
  private class DocumentRepositoryStub implements DocumentRepository {
    final static String IDENTIFIER_STR_1 = "e3cf8742-b595-47f4-8aae-a1e94b62a856";
    final static UUID IDENTIFIER_1 = UUID.fromString(IDENTIFIER_STR_1);
    final static String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    final static Format FORMAT_1 = Format.JSON;
    final static Document DOCUMENT_1 = new Document(IDENTIFIER_1, BASE64_STRING_GRAPH_1, FORMAT_1);

    final static String IDENTIFIER_STR_2 = "dc3b1fc8-d01e-4405-8cf8-94320a11ba4c";
    final static UUID IDENTIFIER_2 = UUID.fromString(IDENTIFIER_STR_2);
    final static String BASE64_STRING_GRAPH_2 = "AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAVwAAAG8AAAByAAAAbAAAAGQAAAAh";
    final static Format FORMAT_2 = Format.JSON;
    final static Document DOCUMENT_2 = new Document(IDENTIFIER_2, BASE64_STRING_GRAPH_2, FORMAT_2);

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
    public Mono<Document> getById(UUID identifier) {
      if (identifier == null)
        return Mono.error(new InternalApplicationException(
            "DocumentNeo4jRepository - Error while reading document",
            new IllegalArgumentException(
                "Identifier can not be 'null'!")));

      switch (identifier.toString()) {
        case IDENTIFIER_STR_1:
          return Mono.just(DOCUMENT_1);
        case IDENTIFIER_STR_2:
          return Mono.just(DOCUMENT_2);
        default:
          return Mono.empty();
      }
    }

    @Override
    public Mono<Void> deleteById(UUID identifier) {
      return Mono.empty().then();
    }

  }

  private final DocumentRepository documentRepository;

  private DocumentServiceImpl documentService;

  @Autowired
  public DocumentServiceImplTest() {
    this.documentRepository = new DocumentRepositoryStub();
  }

  @BeforeEach
  void setUp() {
    documentService = new DocumentServiceImpl(documentRepository);
  }

  @Test
  @Order(1)
  @DisplayName("HappyPath - storeDocument - should return new Document which has been stored.")
  void storeDocument_return_new_document() {

    StepVerifier.create(documentService.storeDocument(DocumentRepositoryStub.DOCUMENT_1))
        .assertNext(doc -> {
          assertEquals(
              DocumentRepositoryStub.IDENTIFIER_STR_1, doc.getIdentifier().toString(),
              "should have exact identifier");
          assertEquals(DocumentRepositoryStub.BASE64_STRING_GRAPH_1, doc.getGraph(),
              "should have exact graph");
          assertEquals(
              DocumentRepositoryStub.FORMAT_1.toString(), doc.getFormat().toString(),
              "should have exact format");
        })
        .verifyComplete();
  }

  @Test
  @Order(2)
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
  @Order(3)
  @DisplayName("HappyPath - getAllDocuments - should return Flux with all documents from repository.")
  void getAllDocuments_should_return_all_documents() {

    StepVerifier.create(documentService.getAllDocuments())
        .assertNext(doc -> {
          assertEquals(DocumentRepositoryStub.IDENTIFIER_STR_1, doc.getIdentifier().toString(),
              "should have exact id");
        })
        .assertNext(doc -> {
          assertEquals(DocumentRepositoryStub.IDENTIFIER_STR_2, doc.getIdentifier().toString(),
              "should have exact id");
        })
        .verifyComplete();
  }

  @Test
  @Order(4)
  @DisplayName("HappyPath - getDocumentById - should return Mono with exact document from repository.")
  void getDocumentById_should_return_mono_with_exact_document() {

    StepVerifier.create(documentService.getDocumentById(DocumentRepositoryStub.IDENTIFIER_1))
        .assertNext(doc -> {
          assertEquals(DocumentRepositoryStub.IDENTIFIER_STR_1, doc.getIdentifier().toString(),
              "should have exact id");
        })
        .verifyComplete();
  }

  @Test
  @Order(5)
  @DisplayName("HappyPath - getDocumentById - should return empty Mono.")
  void getDocumentById_should_return_empty_mono() {

    StepVerifier.create(documentService.getDocumentById(UUID.randomUUID()))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  @Order(6)
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
              "Identifier can not be 'null'!",
              err.getCause().getMessage(),
              "should have exact error message");
        });
  }

  @Test
  @Order(7)
  @DisplayName("HappyPath - deleteById - should return empty Mono.")

  void deleteDocumentById_should_delete_document() {

    StepVerifier.create(documentService.deleteDocumentById(DocumentRepositoryStub.IDENTIFIER_1))
        .verifyComplete();
  }
}